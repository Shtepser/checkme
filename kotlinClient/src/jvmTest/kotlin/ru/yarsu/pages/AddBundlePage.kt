package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.baseUrl
import ru.yarsu.TestConfig.waitForElement

class AddBundlePage(private val driver: WebDriver) {
    fun open() {
        driver.get("$baseUrl/#/add-bundle")
        Thread.sleep(1000)
    }

    fun createBundle(name: String) {
        waitForElement(driver, By.id("add-bundle-name")).sendKeys(name)
        waitForElement(driver, By.id("add-bundle-submit-button")).click()
        Thread.sleep(2000)
    }

    fun isBundleCreated(): Boolean {
        return try {
            driver.findElement(By.id("add-tasks-bundle-button")).isDisplayed ||
                    driver.findElement(By.id("edit-tasks-bundle-button")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}