package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class AddBundlePage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun open() {
        driver.get("http://localhost:8080/#/add-bundle")
        Thread.sleep(1000)
    }

    fun createBundle(name: String) {
        waitForElement(By.id("add-bundle-name")).sendKeys(name)
        waitForElement(By.id("add-bundle-submit-button")).click()
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