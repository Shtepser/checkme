package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class MyResultListPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun open() {
        driver.get("http://localhost:8080/#/my-result-list/1")
        Thread.sleep(1000)
    }

    fun isPageLoaded(): Boolean {
        return try {
            driver.findElement(By.id("my-solution-h2")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun isBundleNameVisible(bundleName: String): Boolean {
        return try {
            val bundleHeaders = driver.findElements(By.cssSelector(".bundle-name"))
            bundleHeaders.any { it.text.contains(bundleName) }
        } catch (_: Exception) {
            false
        }
    }

    fun isTaskBlockVisible(taskName: String): Boolean {
        return try {
            val taskBlocks = driver.findElements(By.cssSelector("[id^='task-block-']"))
            taskBlocks.any { it.text.contains(taskName) }
        } catch (_: Exception) {
            false
        }
    }

    fun openTaskSolutions(taskName: String) {
        val taskBlocks = driver.findElements(By.cssSelector("[id^='task-block-']"))
        val taskBlock = taskBlocks.find { it.text.contains(taskName) }
        if (taskBlock != null) {
            taskBlock.click()
            Thread.sleep(1000)
        } else {
            throw Exception("Блок задачи '$taskName' не найден")
        }
    }

    fun isTaskBlockHasNoSolutions(taskName: String): Boolean {
        return try {
            val taskBlocks = driver.findElements(By.cssSelector("[id^='task-block-']"))
            val taskBlock = taskBlocks.find { it.text.contains(taskName) }
            taskBlock?.text?.contains("Нет решений") == true
        } catch (_: Exception) {
            false
        }
    }
}