import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    application
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.shadowJar)
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.flywayPostgresql)
        classpath(libs.shadowJar)
    }
}

repositories {
    mavenCentral()
}

sourceSets["main"].kotlin {
    srcDir("src/main/kotlin-generated")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
        jvmTarget = JvmTarget.JVM_21
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass = "checkme.ServerKt"
}

dependencies {
    jooqCodegen(libs.bundles.codegen)
    implementation(libs.kotlinStdlib)
    implementation(libs.bundles.http4k)
    implementation(libs.result4k)
    implementation(libs.bundles.jooq)
    implementation(libs.postgreSql)
    implementation(libs.hikariConnectionPool)
    implementation(libs.bundles.logging)
    implementation(libs.jwtJava)
    implementation(libs.bundles.jackson)
    implementation(libs.flexmark)
    implementation(libs.flywayPostgresql)
    implementation(libs.bundles.apachePoi)
    implementation(libs.bundles.scribeJava)
    implementation(libs.mariaDb)
    implementation(libs.jacksonDataType)
    implementation(libs.csvPareser)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.http4kTesting)
    testImplementation(libs.mockito)
    testImplementation(libs.result4kKotest)
    testImplementation(libs.bundles.testcontainers)
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.withType<ShadowJar> {
    mergeServiceFiles {
        setPath("META-INF/services/org.flywaydb.core.extensibility.Plugin")
    }
    manifest {
        attributes["Main-Class"] = "checkme.ServerKt"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveBaseName = "checkme"
    }

    if (!project.hasProperty("checkme")) exclude("checkme/**")

    configurations = listOf(project.configurations["compileClasspath"])
}

val appProperties =
    Properties()
        .apply {
            val propertiesFile = project.file("app.properties")
            if (propertiesFile.exists()) {
                load(propertiesFile.reader())
            }
        }

val dbHost: String = appProperties.getProperty("db.host", "localhost")
val dbPort: String = appProperties.getProperty("db.port", "5432")
val dbName: String = appProperties.getProperty("db.base", "checkmedb")
val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
val dbUser: String = appProperties.getProperty("db.user", "postgres")
val dbPassword: String = appProperties.getProperty("db.password", "secretpassword")

flyway {
    url = jdbcUrl
    user = dbUser
    password = dbPassword
    locations = arrayOf("classpath:checkme/db/migrations")
    cleanDisabled = false
    validateMigrationNaming = true
}

tasks {
    named { it.startsWith("flyway") }.forEach {
        it.dependsOn(classes)
    }
}

jooq {
    configuration {
        jdbc {
            url = jdbcUrl
            username = dbUser
            password = dbPassword
        }

        generator {
            name = "org.jooq.codegen.KotlinGenerator"

            database {
                includes = ".*"
                excludes = "flyway_schema_history"
                inputSchema = "public"
                catalogVersionProvider = """
                    SELECT MAX(version) FROM flyway_schema_history
                """.trimMargin()
                schemaVersionProvider = """
                    SELECT MAX(version) FROM flyway_schema_history
                """.trimMargin()
            }

            target {
                packageName = "checkme.db.generated"
                directory = "src/main/kotlin-generated"
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kover {
    reports {
        filters {
            excludes {
                classes("*.generated.*")

                classes("checkme.ServerKt")

                classes("checkme.db.utils.*")

                classes("checkme.web.meta.*")
                classes("checkme.web.*models.*")
                classes("checkme.web.*Routes*")
            }
        }
    }
}

ktlint {
    version = libs.versions.ktlint
    verbose = true
    android = false
    outputToConsole = true

    filter {
        exclude("**/generated/**")
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file(".detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.register<JavaExec>("cleanDatabase") {
    group = "database"
    description = "Удаление пользователей с ролью STUDENT и их решения"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("gradleTasks.CleanerDatabaseKt")
}
