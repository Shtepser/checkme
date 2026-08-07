import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.DriverManager
import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Добавляем драйвер PostgreSQL в classpath скрипта сборки
        classpath("org.postgresql:postgresql:42.7.3")
    }
}

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

// ========== Configuration ==========
val testDbBase = "checkme_test_${System.currentTimeMillis()}"

// ========== Helper Functions ==========

fun readServerDbConfig(projectDir: File): Properties {
    val projectRoot = projectDir.parentFile
    val serverDir = File(projectRoot, "kotlinServer")
    val propertiesFile = File(serverDir, "app.properties")

    if (!propertiesFile.exists()) {
        throw GradleException("app.properties file not found in $serverDir")
    }

    val props = Properties()
    FileInputStream(propertiesFile).use { props.load(it) }
    return props
}

fun createTestDatabase(host: String, port: String, user: String, password: String, testBase: String) {
    println("[DB] Creating test database: $testBase")
    val adminUrl = "jdbc:postgresql://$host:$port/postgres"

    DriverManager.getConnection(adminUrl, user, password).use { conn ->
        conn.autoCommit = true
        conn.createStatement().use { stmt ->
            try {
                stmt.execute("""
                    SELECT pg_terminate_backend(pid) 
                    FROM pg_stat_activity 
                    WHERE datname = '$testBase' AND pid <> pg_backend_pid()
                """.trimIndent())
            } catch (_: Exception) { }

            try {
                stmt.execute("DROP DATABASE IF EXISTS $testBase")
            } catch (_: Exception) { }

            stmt.execute("CREATE DATABASE $testBase")
        }
    }
    println("[DB] Test database created: $testBase")
}

fun dropTestDatabase(host: String, port: String, user: String, password: String, testBase: String) {
    println("[DB] Dropping test database: $testBase")
    val adminUrl = "jdbc:postgresql://$host:$port/postgres"

    try {
        DriverManager.getConnection(adminUrl, user, password).use { conn ->
            conn.autoCommit = true
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    SELECT pg_terminate_backend(pid) 
                    FROM pg_stat_activity 
                    WHERE datname = '$testBase' AND pid <> pg_backend_pid()
                """.trimIndent())
                stmt.execute("DROP DATABASE IF EXISTS $testBase")
            }
        }
        println("[DB] Test database dropped: $testBase")
    } catch (e: Exception) {
        println("[DB] Warning: Failed to drop test database: ${e.message}")
    }
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

// Recursively kills a process and all its children (releases ports properly)
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
        println("⚠ Warning during process kill: ${e.message}")
    }
}

// Streams process output line by line with a prefix
fun streamProcessOutput(process: Process, prefix: String) {
    Thread {
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println("[$prefix] $line")
            }
        } catch (e: Exception) {
            // Process ended
        }
    }.apply {
        isDaemon = true
        start()
    }
}

// ========== e2eTest Task ==========

tasks.register("e2eTest") {
    group = "verification"
    description = "Runs the server with a test DB, the client, and E2E tests"

    doLast {
        val headless = project.findProperty("headless")?.toString()?.toBoolean() ?: true
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val gradleWrapper = if (isWindows) "gradlew.bat" else "./gradlew"

        val projectRoot = projectDir.parentFile
        val serverDir = File(projectRoot, "kotlinServer")
        val clientDir = projectDir

        println("[CONFIG] Reading DB configuration from kotlinServer/app.properties...")
        val props = readServerDbConfig(projectDir)

        val dbHost = props.getProperty("db.host") ?: "localhost"
        val dbPort = props.getProperty("db.port") ?: "5433"
        val dbUser = props.getProperty("db.user") ?: "postgres"
        val dbPassword = props.getProperty("db.password") ?: ""
        val originalDbBase = props.getProperty("db.base") ?: "checkMeActual"

        println("[CONFIG] Original DB: $originalDbBase")
        println("[CONFIG] Test DB: $testDbBase")
        println("[CONFIG] Host: $dbHost:$dbPort")

        try {
            createTestDatabase(dbHost, dbPort, dbUser, dbPassword, testDbBase)

            println("[SERVER] Starting server with test DB...")
            val serverProcessBuilder = ProcessBuilder(gradleWrapper, "run")
                .directory(serverDir)
                .redirectErrorStream(true)

            val env = serverProcessBuilder.environment()
            env["db.host"] = dbHost
            env["db.port"] = dbPort
            env["db.user"] = dbUser
            env["db.password"] = dbPassword
            env["db.base"] = testDbBase

            val serverProcess = serverProcessBuilder.start()
            streamProcessOutput(serverProcess, "SERVER")

            println("[CLIENT] Starting client...")
            // Changed from jsRun to run
            val clientProcess = ProcessBuilder(gradleWrapper, "run")
                .directory(clientDir)
                .redirectErrorStream(true)
                .start()
            streamProcessOutput(clientProcess, "CLIENT")

            try {
                val serverPort = readServerPort(projectDir)
                val serverUrl = "http://localhost:$serverPort"

                println("[SERVER] Waiting for server readiness ($serverUrl)...")
                if (!waitForServer(serverUrl)) {
                    throw GradleException("Server did not start within 60 seconds")
                }
                println("[SERVER] Server is ready")

                println("[CLIENT] Waiting for Webpack compilation (60 sec)...")
                Thread.sleep(60000)
                println("[CLIENT] Client is fully ready")

                println("[TEST] Running tests...")
                val testProcess = ProcessBuilder(
                    gradleWrapper, "jvmTest", "-Dtest.headless=$headless"
                )
                    .directory(clientDir)
                    .inheritIO()
                    .start()

                val testExitCode = testProcess.waitFor()

                if (testExitCode != 0) {
                    println("\n[TEST] Tests finished with error (code $testExitCode)")
                    throw GradleException("Tests finished with error")
                }

                println("\n[TEST] Tests passed successfully!")
            } finally {
                println("\n[STOP] Stopping server and client processes...")
                killProcessTree(clientProcess, isWindows)
                killProcessTree(serverProcess, isWindows)

                clientProcess.waitFor(5, TimeUnit.SECONDS)
                serverProcess.waitFor(5, TimeUnit.SECONDS)
                println("[STOP] Processes stopped")
            }
        } finally {
            dropTestDatabase(dbHost, dbPort, dbUser, dbPassword, testDbBase)
        }
    }
}
