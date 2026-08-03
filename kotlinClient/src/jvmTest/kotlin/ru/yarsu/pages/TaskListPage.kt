package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement

class TaskListPage(private val driver: WebDriver) {
    fun open() {
        driver.get("http://localhost:8080/#/tasks/all")
        Thread.sleep(1000)
    }

    fun navigateToCreateTask() {
        waitForElement(driver, By.id("create-task-button")).click()
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

    fun isTaskInList(taskName: String): Boolean {
        return try {
            val taskElements = driver.findElements(By.cssSelector(".task-item"))
            taskElements.any { it.text.contains(taskName) }
        } catch (_: Exception) {
            false
        }
    }
}