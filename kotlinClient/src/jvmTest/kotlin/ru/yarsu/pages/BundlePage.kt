package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class BundlePage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun getBundleName(): String = driver.findElement(By.tagName("h2")).text


    fun navigateToAddTasks() {
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
        waitForElement(By.id("delete-bundle-button")).click()
        Thread.sleep(2000)
    }

    fun isBundlePageClosed(): Boolean {
        return try {
            !driver.findElement(By.id("delete-bundle-button")).isDisplayed
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