package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.baseUrl
import ru.yarsu.TestConfig.waitForElement

class AddBundlePage(private val driver: WebDriver) {
    fun open() {
        driver.get("$baseUrl/#/")
        waitForElement(driver, By.id("create-bundle-button")).click()
        Thread.sleep(1000)
    }

    fun createBundle(name: String) {
        waitForElement(driver, By.id("add-bundle-name")).sendKeys(name)
        waitForElement(driver, By.id("add-bundle-submit-button")).click()
        Thread.sleep(2000)
    }

    fun isBundleCreated(): Boolean {
        return try {
            driver.findElement(By.id("bundle-actions-dropdown")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}