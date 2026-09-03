package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.baseUrl

class SignInPage(private val driver: WebDriver) {
    fun open() {
        driver.get("$baseUrl/#/authorization/sign_in")
    }

    fun login(username: String, password: String) {
        driver.findElement(By.id("sign-in-username")).sendKeys(username)
        driver.findElement(By.id("sign-in-password")).sendKeys(password)
        driver.findElement(By.id("sign-in-button")).click()
    }

    fun switchToSignUpPage() {
        driver.findElement(By.id("sign-in-switch-button")).click()
    }

    fun isLoginSuccessful(): Boolean {
        return try {
            driver.findElement(By.id("header")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}