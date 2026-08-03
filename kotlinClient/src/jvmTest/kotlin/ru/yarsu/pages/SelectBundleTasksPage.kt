package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class SelectBundleTasksPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

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
        waitForElement(By.id("next-button")).click()
        Thread.sleep(1500)
    }
}