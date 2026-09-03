package ru.yarsu

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ru.yarsu.TestConfig.driver
import ru.yarsu.pages.AddBundlePage
import ru.yarsu.pages.AddTaskPage
import ru.yarsu.pages.BundleListPage
import ru.yarsu.pages.BundlePage
import ru.yarsu.pages.ChangeBundleTasksOrderPage
import ru.yarsu.pages.HeaderComponent
import ru.yarsu.pages.SelectBundleTasksPage
import ru.yarsu.pages.SignInPage
import ru.yarsu.pages.TaskListPage
import ru.yarsu.pages.TaskPage
import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BundleTest : BaseTest() {
    private val signInPage by lazy { SignInPage(driver) }
    private val headerComponent by lazy { HeaderComponent(driver) }
    private val bundleListPage by lazy { BundleListPage(driver) }
    private val addBundlePage by lazy { AddBundlePage(driver) }
    private val bundlePage by lazy { BundlePage(driver) }
    private val selectBundleTasksPage by lazy { SelectBundleTasksPage(driver) }
    private val changeBundleTasksOrderPage by lazy { ChangeBundleTasksOrderPage(driver) }
    private val addTaskPage by lazy { AddTaskPage(driver) }
    private val taskListPage by lazy { TaskListPage(driver) }
    private val taskPage by lazy { TaskPage(driver) }

    private val adminUsername = "admin"
    private val adminPassword = "pass"

    private val bundleName = "Test Bundle ${System.currentTimeMillis()}"
    private val consoleTaskName = "Console Task ${System.currentTimeMillis()}"
    private val sqlTaskName = "SQL Task ${System.currentTimeMillis()}"

    private val jsonConsoleCriterionFilePath = "src/jvmTest/resources/test-console-criterion.json"
    private val jsonSqlCriterionFilePath = "src/jvmTest/resources/test-sql-criterion.json"
    private val sqlScriptFilePath = "src/jvmTest/resources/test-script.sql"

    @Test
    @Order(1)
    fun `admin login`() {
        signInPage.open()
        signInPage.login(adminUsername, adminPassword)

        await().atMost(5, TimeUnit.SECONDS).until {
            headerComponent.isHeaderDisplayed()
        }

        assertTrue(headerComponent.isHeaderDisplayed(), "Header должен отображаться после входа")
    }

    @Test
    @Order(2)
    fun `create console task for bundle`() {
        addTaskPage.open()

        addTaskPage.createConsoleTask(
            consoleTaskName,
            "Описание консольной задачи для теста набора",
            jsonConsoleCriterionFilePath
        )

        await().atMost(5, TimeUnit.SECONDS).until {
            addTaskPage.isTaskCreated()
        }

        assertTrue(addTaskPage.isTaskCreated(), "Консольная задача должна быть создана")
    }

    @Test
    @Order(3)
    fun `create sql task for bundle`() {
        addTaskPage.open()

        addTaskPage.createSqlTask(
            sqlTaskName,
            "Описание SQL задачи для теста набора",
            jsonSqlCriterionFilePath,
            sqlScriptFilePath
        )

        await().atMost(5, TimeUnit.SECONDS).until {
            addTaskPage.isTaskCreated()
        }

        assertTrue(addTaskPage.isTaskCreated(), "SQL задача должна быть создана")
    }

    @Test
    @Order(4)
    fun `create bundle`() {
        headerComponent.navigateToBundleList()

        bundleListPage.navigateToCreateBundle()

        addBundlePage.createBundle(bundleName)

        await().atMost(5, TimeUnit.SECONDS).until {
            addBundlePage.isBundleCreated()
        }

        assertTrue(addBundlePage.isBundleCreated(), "Набор должен быть создан")
    }

    @Test
    @Order(5)
    fun `add tasks to bundle and save order`() {
        bundlePage.navigateToAddTasks()

        selectBundleTasksPage.selectTask(consoleTaskName)
        selectBundleTasksPage.selectTask(sqlTaskName)

        selectBundleTasksPage.saveSelection()

        changeBundleTasksOrderPage.saveOrder()

        await().atMost(5, TimeUnit.SECONDS).until {
            changeBundleTasksOrderPage.isOrderSaved()
        }

        assertTrue(changeBundleTasksOrderPage.isOrderSaved(), "Порядок должен быть сохранен")
    }

    @Test
    @Order(6)
    fun `verify tasks in bundle`() {
        await().atMost(5, TimeUnit.SECONDS).until {
            bundlePage.isTaskInBundle(consoleTaskName)
        }
        await().atMost(5, TimeUnit.SECONDS).until {
            bundlePage.isTaskInBundle(sqlTaskName)
        }

        assertTrue(bundlePage.isTaskInBundle(consoleTaskName), "Консольная задача должна быть в наборе")
        assertTrue(bundlePage.isTaskInBundle(sqlTaskName), "SQL задача должна быть в наборе")
    }

    @Test
    @Order(7)
    fun `verify bundle in list`() {
        headerComponent.navigateToBundleList()

        await().atMost(5, TimeUnit.SECONDS).until {
            bundleListPage.isBundleInList(bundleName)
        }

        assertTrue(bundleListPage.isBundleInList(bundleName), "Набор должен быть в списке")
    }

    @Test
    @Order(8)
    fun `delete bundle`() {
        bundleListPage.openBundle(bundleName)

        bundlePage.deleteBundle()

        await().atMost(5, TimeUnit.SECONDS).until {
            bundlePage.isBundlePageClosed()
        }
        assertTrue(bundlePage.isBundlePageClosed(), "После удаления набора должен произойти редирект")

        headerComponent.navigateToBundleList()
        await().atMost(5, TimeUnit.SECONDS).until {
            !bundleListPage.isBundleInList(bundleName)
        }
        assertTrue(!bundleListPage.isBundleInList(bundleName), "Набор должен пропасть из списка")
    }

    @Test
    @Order(9)
    fun `delete tasks separately`() {
        headerComponent.navigateToTaskList()

        taskListPage.openTask(consoleTaskName)
        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.getTaskName().contains(consoleTaskName)
        }
        taskPage.deleteTask()
        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.isTaskPageClosed()
        }

        headerComponent.navigateToTaskList()

        taskListPage.openTask(sqlTaskName)
        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.getTaskName().contains(sqlTaskName)
        }
        taskPage.deleteTask()
        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.isTaskPageClosed()
        }

        headerComponent.navigateToTaskList()
        await().atMost(5, TimeUnit.SECONDS).until {
            !taskListPage.isTaskInList(consoleTaskName)
        }
        await().atMost(5, TimeUnit.SECONDS).until {
            !taskListPage.isTaskInList(sqlTaskName)
        }

        assertTrue(!taskListPage.isTaskInList(consoleTaskName), "Консольная задача должна быть удалена")
        assertTrue(!taskListPage.isTaskInList(sqlTaskName), "SQL задача должна быть удалена")
    }
}