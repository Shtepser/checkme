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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

buildscript {
    repositories {
        mavenCentral()
    }
    val postgresVersion = "42.7.3"
    dependencies {
        classpath("org.postgresql:postgresql:$postgresVersion")
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

    val headless = project.findProperty("headless")?.toString()?.toBoolean() ?: true
    systemProperty("test.headless", headless)

    val browser = project.findProperty("browser")?.toString() ?: "firefox"
    systemProperty("test.browser", browser)

    outputs.upToDateWhen { false }
}

val testDB = "checkme_test_${System.currentTimeMillis()}"

fun readServerDBConfig(projectDir: File): Properties {
    val projectRoot = projectDir.parentFile
    val serverDir = File(projectRoot, "kotlinServer")
    val propertiesFile = File(serverDir, "app.properties")

    if (!propertiesFile.exists()) {
        throw GradleException("app.properties file not found in $serverDir")
    }

    val properties = Properties()
    FileInputStream(propertiesFile).use { properties.load(it) }
    return properties
}

fun readServerPort(projectDir: File): String {
    val file = File(projectDir, "webpack.config.d/app.properties.js")
    if (!file.exists()) {
        println("Warning: app.properties.js not found. Defaulting to port 9999")
        return "9999"
    }
    val content = file.readText()
    val regex = Regex("const\\s+server_url\\s*=\\s*['\"]http://localhost:(\\d+)")
    val match = regex.find(content)
    return if (match != null) {
        match.groupValues[1]
    } else {
        println("Warning: Could not parse server_url from ${file.name}. Defaulting to 9999")
        "9999"
    }
}

fun createTestDB(host: String, port: String, user: String, password: String, testBase: String) {
    println("Creating test database: $testBase")
    val adminUrl = "jdbc:postgresql://$host:$port/postgres"
    DriverManager.getConnection(adminUrl, user, password).use { conn ->
        conn.autoCommit = true
        conn.createStatement().use { stmt ->
            stmt.execute("CREATE DATABASE $testBase")
        }
    }
    println("Test database created: $testBase")
}

fun dropTestDatabase(host: String, port: String, user: String, password: String, testBase: String) {
    println("Dropping test database: $testBase")
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
        println("Test database dropped: $testBase")
    } catch (e: Exception) {
        println("Warning: Failed to drop test database: ${e.message}")
    }
}

fun waitForServer(url: String, timeoutMs: Long = 60000): Boolean {
    val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()
    val request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build()
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeoutMs) {
        try {
            client.send(request, HttpResponse.BodyHandlers.discarding())
            return true
        } catch (_: Exception) { }
        Thread.sleep(1000)
    }
    return false
}

fun killProcessTree(process: Process) {
    try {
        val handle = process.toHandle()
        handle.descendants().forEach { descendant ->
            descendant.destroyForcibly()
        }
        handle.destroyForcibly()
    } catch (e: Exception) {
        println("Warning during process kill: ${e.message}")
    }
}

fun streamProcessOutput(
    process: Process,
    prefix: String,
    watchRegex: Regex? = null,
    readyLatch: CountDownLatch? = null
) {
    Thread {
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println("[$prefix] $line")
                if ((watchRegex != null) && (readyLatch != null) && (line?.contains(watchRegex) ?: false)) {
                    readyLatch.countDown()
                }
            }
        } catch (_: Exception) { }
    }.apply {
        isDaemon = true
        start()
    }
}

tasks.register("clientTest") {
    group = "verification"
    description = "Runs the server with a test DB, the client and client tests"

    doLast {
        val headless = project.findProperty("headless")?.toString()?.toBoolean() ?: true
        val browser = project.findProperty("browser")?.toString() ?: "firefox"
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val gradleWrapper = if (isWindows) "gradlew.bat" else "./gradlew"

        val serverDir = File(projectDir.parentFile, "kotlinServer")
        val clientDir = projectDir

        val properties = readServerDBConfig(projectDir)
        val dbHost = properties.getProperty("db.host") ?: "localhost"
        val dbPort = properties.getProperty("db.port") ?: "5432"
        val dbUser = properties.getProperty("db.user") ?: "postgres"
        val dbPassword = properties.getProperty("db.password") ?: ""

        println("Test DB: $testDB")
        println("Host: $dbHost:$dbPort")

        try {
            createTestDB(dbHost, dbPort, dbUser, dbPassword, testDB)
            println("Starting server with test DB")
            val serverProcessBuilder = ProcessBuilder(gradleWrapper, "run")
                .directory(serverDir)
                .redirectErrorStream(true)

            val env = serverProcessBuilder.environment()
            env["db.host"] = dbHost
            env["db.port"] = dbPort
            env["db.user"] = dbUser
            env["db.password"] = dbPassword
            env["db.base"] = testDB

            val serverProcess = serverProcessBuilder.start()
            streamProcessOutput(serverProcess, "SERVER")

            println("Starting client")
            val clientProcess = ProcessBuilder(gradleWrapper, "run")
                .directory(clientDir)
                .redirectErrorStream(true)
                .start()
            val clientReadyLatch = CountDownLatch(1)
            streamProcessOutput(
                clientProcess,
                "CLIENT",
                Regex("(?i)compiled successfully"),
                clientReadyLatch
            )

            try {
                val serverPort = readServerPort(projectDir)
                val serverUrl = "http://localhost:$serverPort"

                println("Waiting for server ($serverUrl) readiness")
                if (!waitForServer(serverUrl)) {
                    throw GradleException("Server did not start within 60 seconds")
                }
                println("Server is ready")

                println("Waiting for Webpack compilation")
                val isClientReady = clientReadyLatch.await(90, TimeUnit.SECONDS)
                if (!isClientReady) {
                    throw GradleException("Client did not compile successfully within 90 seconds")
                }
                println("Client is ready")

                println("Running tests")
                val testProcess = ProcessBuilder(
                    gradleWrapper,
                    "jvmTest",
                    "-Pheadless=$headless",
                    "-Pbrowser=$browser",
                    "--stacktrace"
                )
                    .directory(clientDir)
                    .redirectErrorStream(true)
                    .start()
                streamProcessOutput(testProcess, "TEST")

                val testExitCode = testProcess.waitFor()
                if (testExitCode != 0) {
                    throw GradleException("Tests finished with error (code $testExitCode)")
                }
                println("\nTests passed successfully!")
            } finally {
                println("\nStopping server and client processes")
                killProcessTree(clientProcess)
                killProcessTree(serverProcess)

                clientProcess.waitFor(5, TimeUnit.SECONDS)
                serverProcess.waitFor(5, TimeUnit.SECONDS)
                println("Processes stopped")
            }
        } finally {
            dropTestDatabase(dbHost, dbPort, dbUser, dbPassword, testDB)
        }
    }
}
