package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class BundleListPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun open() {
        driver.get("http://localhost:8080/#/")
        Thread.sleep(1000)
    }

    fun navigateToCreateBundle() {
        waitForElement(By.id("create-bundle-button")).click()
        Thread.sleep(1000)
    }

    fun isBundleInList(bundleName: String): Boolean {
        return try {
            val bundleElements = driver.findElements(By.cssSelector(".bundle-item"))
            bundleElements.any { it.text.contains(bundleName) }
        } catch (_: Exception) {
            false
        }
    }

    fun openBundle(bundleName: String) {
        val bundleElements = driver.findElements(By.cssSelector(".bundle-item"))
        val bundle = bundleElements.find { it.text.contains(bundleName) }
        if (bundle != null) {
            bundle.click()
            Thread.sleep(1000)
        } else {
            throw Exception("Набор '$bundleName' не найден в списке")
        }
    }
}