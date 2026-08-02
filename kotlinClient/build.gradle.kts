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
}
