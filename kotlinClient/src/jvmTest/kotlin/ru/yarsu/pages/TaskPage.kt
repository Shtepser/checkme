package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
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

    fun uploadSolution(filePath: String) {
        val fileInput = driver.findElement(By.id("input-solution-file"))
        fileInput.sendKeys(File(filePath).absolutePath)
        Thread.sleep(1000)

        val sendButton = waitForElement(By.id("task-send-button"))
        sendButton.click()
        Thread.sleep(2000)
    }
}