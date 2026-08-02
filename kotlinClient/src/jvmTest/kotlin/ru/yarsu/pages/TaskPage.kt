package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class TaskPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun getTaskName(): String = driver.findElement(By.id("task-name-h2")).text

    fun deleteTask() {
        waitForElement(By.id("task-delete-button")).click()
        Thread.sleep(2000)
    }

    fun isTaskPageClosed(): Boolean {
        return try {
            !driver.findElement(By.id("task-name-h2")).isDisplayed
        } catch (_: Exception) {
            true
        }
    }
}