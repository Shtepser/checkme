package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement

class SelectBundleTasksPage(private val driver: WebDriver) {
    fun selectTask(taskName: String) {
        val taskElements = driver.findElements(By.cssSelector(".task-item"))
        val task = taskElements.find { it.text.contains(taskName) }
        if (task != null) {
            task.click()
            Thread.sleep(500)
        } else {
            throw Exception("Задача '$taskName' не найдена для выбора")
        }
    }

    fun saveSelection() {
        waitForElement(driver, By.id("next-button")).click()
        Thread.sleep(1500)
    }
}