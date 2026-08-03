package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement

class HeaderComponent(private val driver: WebDriver) {
    fun signOut() {
        waitForElement(driver, By.id("sign-out-button")).click()
        Thread.sleep(1000)
    }

    fun navigateToTaskList() {
        waitForElement(driver, By.id("tasks-navigation")).click()
        Thread.sleep(1000)
    }

    fun navigateToBundleList() {
        waitForElement(driver, By.id("bundle-list-navigation")).click()
        Thread.sleep(1000)
    }

    fun navigateToMyResults() {
        waitForElement(driver, By.id("my-result-list-navigation")).click()
        Thread.sleep(1000)
    }

    fun isHeaderDisplayed() : Boolean {
        return try {
            driver.findElement(By.id("header")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun isUserSignOut() : Boolean {
        return try {
            driver.findElement(By.id("sign-in-form")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}