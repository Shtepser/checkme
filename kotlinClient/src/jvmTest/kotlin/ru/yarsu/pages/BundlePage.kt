package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement

class BundlePage(private val driver: WebDriver) {
    fun navigateToAddTasks() {
        waitForElement(driver, By.id("bundle-actions-dropdown")).click()
        val addTasksButton = driver.findElements(By.id("add-tasks-bundle-button")).firstOrNull()
            ?: driver.findElements(By.id("edit-tasks-bundle-button")).firstOrNull()

        if (addTasksButton != null) {
            addTasksButton.click()
        } else {
            throw Exception("Кнопка добавления/изменения задач не найдена")
        }
        Thread.sleep(1000)
    }

    fun deleteBundle() {
        waitForElement(driver, By.id("bundle-actions-dropdown")).click()
        waitForElement(driver, By.id("delete-bundle-button")).click()
        Thread.sleep(2000)
    }

    fun isBundlePageClosed(): Boolean {
        return try {
            !driver.findElement(By.id("bundle-actions-dropdown")).isDisplayed
        } catch (_: Exception) {
            true
        }
    }

    fun isTaskInBundle(taskName: String): Boolean {
        return try {
            val taskElements = driver.findElements(By.cssSelector(".bundle-item"))
            taskElements.any { it.text.contains(taskName) }
        } catch (_: Exception) {
            false
        }
    }
}