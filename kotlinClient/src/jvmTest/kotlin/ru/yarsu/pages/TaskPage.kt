package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement
import java.io.File

class TaskPage(private val driver: WebDriver) {
    fun getTaskName(): String = driver.findElement(By.id("task-name-h2")).text

    fun deleteTask() {
        waitForElement(driver, By.id("task-delete-button")).click()
        Thread.sleep(2000)
    }

    fun uploadSolution(filePath: String) {
        val fileInput = driver.findElement(By.id("input-solution-file"))
        fileInput.sendKeys(File(filePath).absolutePath)
        Thread.sleep(1000)

        val sendButton = waitForElement(driver, By.id("task-send-button"))
        sendButton.click()
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