package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement

class ChangeBundleTasksOrderPage(private val driver: WebDriver) {
    fun saveOrder() {
        waitForElement(driver, By.id("save-task-order-button")).click()
        Thread.sleep(2000)
    }

    fun isOrderSaved(): Boolean {
        return try {
            waitForElement(driver, By.id("bundle-actions-dropdown")).click()
            driver.findElement(By.id("edit-tasks-bundle-button")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}