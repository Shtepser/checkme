package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.baseUrl
import ru.yarsu.TestConfig.waitForElement

class BundleListPage(private val driver: WebDriver) {
    fun open() {
        driver.get("$baseUrl/#/")
        Thread.sleep(1000)
    }

    fun navigateToCreateBundle() {
        waitForElement(driver, By.id("create-bundle-button")).click()
        Thread.sleep(1000)
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

    fun isBundleInList(bundleName: String): Boolean {
        return try {
            val bundleElements = driver.findElements(By.cssSelector(".bundle-item"))
            bundleElements.any { it.text.contains(bundleName) }
        } catch (_: Exception) {
            false
        }
    }
}