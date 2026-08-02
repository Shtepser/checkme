package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class Header(private val driver: WebDriver) {
    private val signOutButton: WebElement get() = driver.findElement(By.id("sign-out-button"))
    private val header: WebElement get() = driver.findElement(By.id("header"))

    fun isUserSignIn() : Boolean {
        return try {
            header.isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun signOut() {
        signOutButton.click()
    }

    fun isUserSignOut() : Boolean {
        return try {
            driver.findElement(By.id("sign-in-form")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}