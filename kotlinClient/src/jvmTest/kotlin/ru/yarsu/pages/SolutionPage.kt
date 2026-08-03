package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class SolutionPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.visibilityOfElementLocated(selector))
    }

    fun isResultDisplayed(): Boolean {
        return try {
            driver.findElement(By.id("solution-score")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun isCriteriaListVisible(): Boolean {
        return try {
            driver.findElement(By.id("title-criteria-list")).isDisplayed
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

    fun getResultScore(): String = driver.findElement(By.id("solution-score")).text

    fun waitForResult(timeoutSeconds: Long = 30): Boolean {
        val wait = WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
        return try {
            wait.until {
                isResultDisplayed() && isCriteriaListVisible()
            }
            true
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