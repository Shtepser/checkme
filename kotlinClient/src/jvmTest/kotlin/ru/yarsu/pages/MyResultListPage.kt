package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.baseUrl

class MyResultListPage(private val driver: WebDriver) {
    fun open() {
        driver.get("$baseUrl/#/my-result-list/1")
        Thread.sleep(1000)
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
}