package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class HeaderComponent(private val driver: WebDriver) {
    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun getUsername() : String = driver.findElement(By.id("username")).text

    fun isHeaderDisplayed() : Boolean {
        return try {
            driver.findElement(By.id("header")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun isUserSignedOut() : Boolean {
        return try {
            driver.findElement(By.id("sign-in-form")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun signOut() {
        waitForElement(By.id("sign-out-button")).click()
        Thread.sleep(1000)
    }

    fun navigateToTasksList() {
        waitForElement(By.id("tasks-navigation")).click()
        Thread.sleep(1000)
    }
}