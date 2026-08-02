package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import ru.yarsu.TestConfig.baseUrl

class SignInPage(private val driver: WebDriver) {
    private val usernameInput: WebElement get() = driver.findElement(By.cssSelector("#sign-in-username input"))
    private val passwordInput: WebElement get() = driver.findElement(By.cssSelector("#sign-in-password input"))
    private val submitButton: WebElement get() = driver.findElement(By.id("sign-in-button"))

    fun open() {
        driver.get("${baseUrl}/#/authorization/sign_in")
    }

    fun login(username: String, password: String) {
        usernameInput.sendKeys(username)
        passwordInput.sendKeys(password)
        submitButton.click()
    }

    fun isLoginSuccessful(): Boolean {
        return try {
            driver.findElement(By.id("header")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}