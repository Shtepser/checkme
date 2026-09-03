package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.baseUrl

class SignUpPage(private val driver: WebDriver) {
    fun open() {
        driver.get("$baseUrl/#/authorization/sign_up")
    }

    fun register(username: String, name: String, surname: String, password: String) {
        driver.findElement(By.id("sign-up-username")).sendKeys(username)
        driver.findElement(By.id("sign-up-name")).sendKeys(name)
        driver.findElement(By.id("sign-up-surname")).sendKeys(surname)
        driver.findElement(By.id("sign-up-password")).sendKeys(password)
        driver.findElement(By.id("sign-up-password-repeat")).sendKeys(password)
        driver.findElement(By.id("sign-up-button")).click()
    }

    fun isRegistrationSuccessful(): Boolean {
        return try {
            driver.findElement(By.id("header")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}