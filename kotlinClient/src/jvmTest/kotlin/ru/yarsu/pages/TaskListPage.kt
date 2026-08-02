package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class TaskListPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun open() {
        driver.get("http://localhost:8080/#/tasks/all")
        Thread.sleep(1000)
    }

    fun isTaskInList(taskName: String): Boolean {
        return try {
            val taskElements = driver.findElements(By.cssSelector(".task-item"))
            taskElements.any { it.text.contains(taskName) }
        } catch (_: Exception) {
            false
        }
    }

    fun navigateToCreateTask() {
        waitForElement(By.id("create-task-button")).click()
        Thread.sleep(1000)
    }

    fun openTask(taskName: String) {
        val taskElements = driver.findElements(By.cssSelector(".task-item"))
        val task = taskElements.find { it.text.contains(taskName) }
        if (task != null) {
            task.click()
            Thread.sleep(1000)
        } else {
            throw Exception("Задача '$taskName' не найдена в списке")
        }
    }
}