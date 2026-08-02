package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.time.Duration

class AddTaskPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun open() {
        driver.get("http://localhost:8080/#/task/add")
        Thread.sleep(1000)
    }

    fun createConsoleTask(name: String, description: String, jsonCriteriaFilePath: String) {
        waitForElement(By.id("add-task-name")).sendKeys(name)

        waitForElement(By.id("add-task-description")).sendKeys(description)

        val jsonFileInput = driver.findElement(By.id("input-file-0"))
        jsonFileInput.sendKeys(File(jsonCriteriaFilePath).absolutePath)
        Thread.sleep(1000)

        val wait = WebDriverWait(driver, Duration.ofSeconds(5))
        wait.until {
            driver.findElement(By.id("add-task-send")).isEnabled
        }

        driver.findElement(By.id("add-task-send")).click()
        Thread.sleep(2000)
    }

    fun createSqlTask(name: String, description: String, jsonCriteriaFilePath: String, sqlScriptFilePath: String) {
        waitForElement(By.id("add-task-name")).sendKeys(name)

        waitForElement(By.id("add-task-description")).sendKeys(description)

        val jsonFileInput = driver.findElement(By.id("input-file-0"))
        jsonFileInput.sendKeys(File(jsonCriteriaFilePath).absolutePath)
        Thread.sleep(1000)

        val sqlTypeRadio = driver.findElement(By.cssSelector("input[value='file']"))
        sqlTypeRadio.click()
        Thread.sleep(500)

        val sqlFileInput = driver.findElement(By.id("input-file-1"))
        sqlFileInput.sendKeys(File(sqlScriptFilePath).absolutePath)
        Thread.sleep(1000)

        val wait = WebDriverWait(driver, Duration.ofSeconds(5))
        wait.until {
            driver.findElement(By.id("add-task-send")).isEnabled
        }

        driver.findElement(By.id("add-task-send")).click()
        Thread.sleep(2000)
    }

    fun isTaskCreated(): Boolean {
        return try {
            driver.findElement(By.id("task-name-h2")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}
