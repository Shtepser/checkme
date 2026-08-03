package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import ru.yarsu.TestConfig.waitForElement

class MySolutionListPage(private val driver: WebDriver) {
    fun getSolutionCount(): Int {
        return try {
            driver.findElements(By.cssSelector("[id^='solution-block-']")).size
        } catch (_: Exception) {
            0
        }
    }

    fun navigateToTask() {
        waitForElement(driver, By.id("link-to-task")).click()
        Thread.sleep(1000)
    }

    fun isSolutionInList(): Boolean {
        return try {
            val solutionBlocks = driver.findElements(By.cssSelector("[id^='solution-block-']"))
            solutionBlocks.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    fun isNoSolutionsMessage(): Boolean {
        return try {
            driver.findElement(By.id("no-solution")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}