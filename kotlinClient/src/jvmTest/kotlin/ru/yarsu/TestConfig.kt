package ru.yarsu

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.nio.file.Paths
import java.time.Duration

object TestConfig {

    private val propertiesFile = File(Paths.get(System.getProperty("user.dir"), "webpack.config.d", "app.properties.js").toString())

    val clientPort: String by lazy {
        if (!propertiesFile.exists()) {
            println("app.properties.js not found! Using default port 8080")
            "8080"
        } else {
            val content = propertiesFile.readText()
            val regex = Regex("const\\s+client_port\\s*:\\s*number\\s*=\\s*(\\d+)")
            val match = regex.find(content)
            match?.groupValues?.get(1) ?: "8080"
        }
    }

    val baseUrl: String = "http://localhost:$clientPort"

    val isHeadless: Boolean = System.getProperty("test.headless", "true").toBoolean()

    fun waitForElement(driver: WebDriver, selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }
}