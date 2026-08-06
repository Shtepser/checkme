import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.TimeUnit

plugins {
    val kotlinVersion = "2.1.21"
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion = "9.1.0"
    id("io.kvision") version kvisionVersion
}

version = "1.0.0-SNAPSHOT"
group = "com.example"

repositories {
    mavenCentral()
    mavenLocal()
}


val kvisionVersion: String = "9.1.0"
val kotlinxVersion: String = "0.7.1"
val seleniumVersion: String = "4.45.0"
val junitJupiterVersion: String = "6.1.0"
val awaitilityVersion: String = "4.3.0"

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
    js(IR) {
        browser {
            useEsModules()
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
    }

    jvm()

    sourceSets["jsMain"].dependencies {
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-rest:${kvisionVersion}")
        implementation("io.kvision:kvision-routing-navigo-ng:${kvisionVersion}")
        implementation("io.kvision:kvision-toastify:${kvisionVersion}")
        implementation("io.kvision:kvision-bootstrap:${kvisionVersion}")
        implementation("io.kvision:kvision-richtext:${kvisionVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:${kotlinxVersion}")
        implementation("io.kvision:kvision-common-types:${kvisionVersion}")
        implementation("io.kvision:kvision-tabulator:${kvisionVersion}")
    }
    sourceSets["jvmTest"].dependencies {
        implementation("org.seleniumhq.selenium:selenium-java:${seleniumVersion}")
        implementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}")
        runtimeOnly("org.junit.platform:junit-platform-launcher")
        implementation("org.awaitility:awaitility-kotlin:${awaitilityVersion}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    val headlessProp = project.findProperty("headless")?.toString()
        ?: System.getProperty("test.headless")
        ?: "true"

    systemProperty("test.headless", headlessProp)

    outputs.upToDateWhen { false }
}

fun readClientPort(projectDir: File): String {
    val file = File(projectDir, "webpack.config.d/app.properties.js")
    if (!file.exists()) return "8080"
    val regex = Regex("const\\s+client_port\\s*=\\s*(\\d+)")
    return regex.find(file.readText())?.groupValues?.get(1) ?: "8080"
}

fun readServerPort(projectDir: File): String {
    val clientFile = File(projectDir, "webpack.config.d/app.properties.js")
    if (clientFile.exists()) {
        val regex = Regex("const\\s+server_url\\s*=\\s*['\"]http://localhost:(\\d+)['\"]")
        val match = regex.find(clientFile.readText())
        if (match != null) return match.groupValues[1]
    }
    return "9999"
}

fun waitForServer(url: String, timeoutMs: Long = 60000): Boolean {
    val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()
    val request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build()

    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeoutMs) {
        try {
            // Если сервер ответил хоть чем-то (даже 404 Not Found), значит он жив и порт открыт!
            client.send(request, HttpResponse.BodyHandlers.discarding())
            return true
        } catch (_: Exception) {
            // ConnectException означает, что порт еще закрыт
        }
        Thread.sleep(1000)
    }
    return false
}

// Функция для запуска процесса и чтения его логов в реальном времени
fun startProcessWithLogs(command: List<String>, dir: File, prefix: String): Process {
    val process = ProcessBuilder(command)
        .directory(dir)
        .redirectErrorStream(true)
        .start()

    // Запускаем фоновый поток, который читает вывод процесса и печатает его
    Thread {
        process.inputStream.bufferedReader().use { reader ->
            var line = reader.readLine()
            while (line != null) {
                println("$prefix $line")
                line = reader.readLine()
            }
        }
    }.start()

    return process
}

fun killProcessTree(process: Process, isWindows: Boolean) {
    try {
        val handle = process.toHandle()
        handle.descendants().forEach { descendant ->
            descendant.destroyForcibly()
        }
        handle.destroyForcibly()

        if (isWindows && handle.isAlive) {
            Runtime.getRuntime().exec("taskkill /F /T /PID ${handle.pid()}").waitFor()
        }
    } catch (e: Exception) {
        println("⚠️ Warning during process kill: ${e.message}")
    }
}

tasks.register("e2eTest") {
    group = "verification"
    description = "Запускает сервер, клиент и E2E тесты"

    doLast {
        val headless = project.findProperty("headless")?.toString()?.toBoolean() ?: true
        val isWindows = System.getProperty("os.name").lowercase().contains("win")

        val projectRoot = projectDir.parentFile
        val serverDir = File(projectRoot, "kotlinServer")
        val clientDir = projectDir

        val serverCommand = if (isWindows) listOf("cmd", "/c", "gradlew.bat", "run") else listOf("./gradlew", "run")
        val clientCommand = if (isWindows) listOf("cmd", "/c", "gradlew.bat", "run") else listOf("./gradlew", "run")
        val testCommand = if (isWindows) listOf("cmd", "/c", "gradlew.bat", "jvmTest", "-Dtest.headless=$headless") else listOf("./gradlew", "jvmTest", "-Dtest.headless=$headless")

        val clientPort = readClientPort(projectDir)
        val serverPort = readServerPort(projectDir)
        val clientUrl = "http://localhost:$clientPort"
        val serverUrl = "http://localhost:$serverPort"

        println("Run E2E tests (headless=$headless)...")
        println("Server URL: $serverUrl")
        println("Client URL: $clientUrl")

        println("Run server...")
        val serverProcess = startProcessWithLogs(serverCommand, serverDir, "[SERVER]")

        println("Run client...")
        val clientProcess = startProcessWithLogs(clientCommand, clientDir, "[CLIENT]")

        try {
            println("Wait server ($serverUrl)...")
            if (!waitForServer(serverUrl)) {
                throw GradleException("Server did not start within 60 seconds. Look at [SERVER] logs above!")
            }
            println("Server ready")

            println("Client port open. Waiting for Webpack compilation (15 sec)...")
            Thread.sleep(15000)
            println("Client fully ready")

            println("Run tests...")
            // Используем функцию, которая читает логи в реальном времени с префиксом [TEST]
            val testProcess = startProcessWithLogs(testCommand, clientDir, "[TEST]")
            val testExitCode = testProcess.waitFor()

            if (testExitCode != 0) {
                throw GradleException("Tests finish with error (code $testExitCode)")
            }

            println("\nTests successful!")
        } finally {
            println("\nStop server and client process...")

            killProcessTree(clientProcess, isWindows)
            killProcessTree(serverProcess, isWindows)

            clientProcess.waitFor(5, TimeUnit.SECONDS)
            serverProcess.waitFor(5, TimeUnit.SECONDS)

            println("Processes stopped")
        }
    }
}