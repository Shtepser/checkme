package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement

class HeaderComponent(private val driver: WebDriver) {
    fun signOut() {
        waitForElement(driver, By.id("user-menu-dropdown")).click()
        waitForElement(driver, By.id("sign-out-button")).click()
        Thread.sleep(1000)
    }

    fun navigateToTaskList() {
        waitForElement(driver, By.id("tasks-dropdown")).click()
        waitForElement(driver, By.id("tasks-navigation")).click()
        waitForElement(driver, By.id("id-app-title")).click()
        Thread.sleep(1000)
    }

    fun navigateToBundleList() {
        if (isAdmin()) {
            waitForElement(driver, By.id("tasks-dropdown")).click()
            waitForElement(driver, By.id("bundle-list-navigation")).click()
            Thread.sleep(1000)
        } else {
            waitForElement(driver, By.id("bundle-list-navigation-student")).click()
            Thread.sleep(1000)
        }
    }

    fun navigateToMyResults() {
        if (isAdmin()) {
            waitForElement(driver, By.id("solutions-dropdown")).click()
            waitForElement(driver, By.id("my-result-list-navigation")).click()
            Thread.sleep(1000)
        } else {
            waitForElement(driver, By.id("my-result-list-navigation-student")).click()
            Thread.sleep(1000)
        }
    }

    fun isAdmin(): Boolean {
        return try {
            driver.findElement(By.id("tasks-dropdown")).isDisplayed
        } catch (_: Exception) {
            false
        }
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