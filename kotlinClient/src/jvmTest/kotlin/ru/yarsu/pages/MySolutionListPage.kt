package ru.yarsu.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class MySolutionListPage(private val driver: WebDriver) {

    private fun waitForElement(selector: By): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        return wait.until(ExpectedConditions.elementToBeClickable(selector))
    }

    fun isPageLoaded(taskName: String): Boolean {
        return try {
            val header = driver.findElement(By.id("link-to-task"))
            header.text.contains(taskName)
        } catch (_: Exception) {
            false
        }
    }

    fun isSolutionInList(): Boolean {
        return try {
            val solutionBlocks = driver.findElements(By.cssSelector("[id^='solution-block-']"))
            solutionBlocks.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    fun getSolutionCount(): Int {
        return try {
            driver.findElements(By.cssSelector("[id^='solution-block-']")).size
        } catch (_: Exception) {
            0
        }
    }

    fun navigateToTask() {
        waitForElement(By.id("link-to-task")).click()
        Thread.sleep(1000)
    }

    fun isNoSolutionsMessage(): Boolean {
        return try {
            driver.findElement(By.id("no-solution")).isDisplayed
        } catch (_: Exception) {
            false
        }
    }

    fun openSolution(index: Int = 0) {
        val solutionBlocks = driver.findElements(By.cssSelector("[id^='solution-block-']"))
        if (index < solutionBlocks.size) {
            solutionBlocks[index].click()
            Thread.sleep(1000)
        } else {
            throw Exception("Решение с индексом $index не найдено")
        }
    }
}