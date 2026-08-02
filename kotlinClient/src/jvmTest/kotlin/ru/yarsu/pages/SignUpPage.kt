package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import ru.yarsu.TestConfig.baseUrl

class SignUpPage(private val driver: WebDriver) {
    private val usernameInput: WebElement get() = driver.findElement(By.cssSelector("#sign-up-username input"))
    private val nameInput: WebElement get() = driver.findElement(By.cssSelector("#sign-up-name input"))
    private val surnameInput: WebElement get() = driver.findElement(By.cssSelector("#sign-up-surname input"))
    private val passwordInput: WebElement get() = driver.findElement(By.cssSelector("#sign-up-password input"))
    private val passwordRepeatInput: WebElement get() = driver.findElement(By.cssSelector("#sign-up-password-repeat input"))
    private val submitButton: WebElement get() = driver.findElement(By.id("sign-up-button"))

    fun open() {
        driver.get("${baseUrl}/#/authorization/sign_up")
    }

    fun register(username: String, name: String, surname: String, password: String) {
        usernameInput.sendKeys(username)
        nameInput.sendKeys(name)
        surnameInput.sendKeys(surname)
        passwordInput.sendKeys(password)
        passwordRepeatInput.sendKeys(password)
        submitButton.click()
    }

    fun isRegistrationSuccessful(): Boolean {
        return try {
            driver.findElement(By.id("header")).isDisplayed
        } catch (e: Exception) {
            false
        }
    }
}