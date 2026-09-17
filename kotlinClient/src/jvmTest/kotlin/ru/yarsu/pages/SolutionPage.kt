package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

class SolutionPage(private val driver: WebDriver) {
    fun getScore(): Int {
        return try {
            val scoreText = driver.findElement(By.id("solution-score")).text
            val regex = Regex("\\d+")
            regex.find(scoreText)?.value?.toInt() ?: 0
        } catch (_: Exception) {
            0
        }
    }

    fun returnToTask() : Boolean {
        return try {
            val linkToTask = driver.findElement(By.id("link-to-task"))
            linkToTask.click()
            driver.findElement(By.id("task-name-h2")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun isResultDisplayed(): Boolean {
        return try {
            driver.findElement(By.id("criteria-score")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun isLoadingDisplayed(): Boolean {
        return try {
            driver.findElement(By.id("loading-circle")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun isSolutionPageLoaded(): Boolean {
        return try {
            isResultDisplayed() || isLoadingDisplayed()
        } catch (_: Exception) {
            false
        }
    }
}