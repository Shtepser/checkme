package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

class SolutionPage(private val driver: WebDriver) {
    fun isResultDisplayed(): Boolean {
        return try {
            driver.findElement(By.id("solution-score")).isDisplayed
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