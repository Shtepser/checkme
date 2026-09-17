package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.time.Duration

class EditTaskPage(private val driver: WebDriver) {

    fun isEditPageLoaded(): Boolean {
        return try {
            driver.findElement(By.id("h2-edit-task")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun updateTaskName(newName: String) {
        val nameInput = driver.findElement(By.id("add-task-name"))
        nameInput.clear()
        nameInput.sendKeys(newName)
    }

    fun updateTaskDescription(newDescription: String) {
        val descInput = driver.findElement(By.id("add-task-description"))
        descInput.clear()
        descInput.sendKeys(newDescription)
    }

    fun updateCriterions(jsonCriteriaFilePath: String) {
        val jsonFileInput = driver.findElement(By.id("input-file-0"))
        jsonFileInput.sendKeys(File(jsonCriteriaFilePath).absolutePath)
        Thread.sleep(1000)
    }

    fun updateSqlScript(sqlScriptFilePath: String) {
        val editScriptButton = driver.findElement(By.id("edit-task-scripts"))
        editScriptButton.click()
        val sqlFileInput = driver.findElement(By.id("input-file-1"))
        sqlFileInput.sendKeys(File(sqlScriptFilePath).absolutePath)
        Thread.sleep(1000)
    }

    fun saveTask() {
        val wait = WebDriverWait(driver, Duration.ofSeconds(5))
        val sendButton = driver.findElement(By.id("add-task-send"))
        sendButton.click()
        Thread.sleep(2000)
    }
}