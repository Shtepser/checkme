package ru.yarsu

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import ru.yarsu.pages.AddTaskPage
import ru.yarsu.pages.HeaderComponent
import ru.yarsu.pages.SignInPage
import ru.yarsu.pages.TaskListPage
import ru.yarsu.pages.TaskPage
import java.time.Duration
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AddTaskTest {

    private lateinit var driver: WebDriver
    private lateinit var signInPage: SignInPage
    private lateinit var headerComponent: HeaderComponent
    private lateinit var taskListPage: TaskListPage
    private lateinit var addTaskPage: AddTaskPage
    private lateinit var taskPage: TaskPage

    private val adminUsername = "admin"
    private val adminPassword = "pass"

    private val consoleTaskName = "Test Console Task ${System.currentTimeMillis()}"
    private val sqlTaskName = "Test SQL Task ${System.currentTimeMillis()}"

    private val jsonConsoleCriterionFilePath = "src/jvmTest/resources/test-console-criterion.json"
    private val jsonSqlCriterionFilePath = "src/jvmTest/resources/test-sql-criterion.json"
    private val sqlScriptFilePath = "src/jvmTest/resources/test-script.sql"

    @BeforeAll
    fun setUp() {
        val options = ChromeOptions().apply {
            addArguments("--headless", "--window-size=1920,1080", "--disable-gpu", "--no-sandbox")
        }
        driver = ChromeDriver(options)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3))

        signInPage = SignInPage(driver)
        headerComponent = HeaderComponent(driver)
        taskListPage = TaskListPage(driver)
        addTaskPage = AddTaskPage(driver)
        taskPage = TaskPage(driver)
    }

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
    fun `create console task`() {
        headerComponent.navigateToTaskList()

        taskListPage.navigateToCreateTask()

        addTaskPage.createConsoleTask(
            consoleTaskName,
            "Test console task description",
            jsonConsoleCriterionFilePath
        )

        await().atMost(5, TimeUnit.SECONDS).until {
            addTaskPage.isTaskCreated()
        }

        assertTrue(addTaskPage.isTaskCreated(), "После создания задачи должен произойти редирект на страницу задачи")
    }

    @Test
    @Order(3)
    fun `verify console task in list and delete it`() {
        headerComponent.navigateToTaskList()

        await().atMost(5, TimeUnit.SECONDS).until {
            taskListPage.isTaskInList(consoleTaskName)
        }
        assertTrue(taskListPage.isTaskInList(consoleTaskName), "Консольная задача должна быть в списке")

        taskListPage.openTask(consoleTaskName)

        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.getTaskName().contains(consoleTaskName)
        }

        taskPage.deleteTask()

        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.isTaskPageClosed()
        }
        assertTrue(taskPage.isTaskPageClosed(), "После удаления задачи должен произойти редирект")

        headerComponent.navigateToTaskList()
        await().atMost(5, TimeUnit.SECONDS).until {
            !taskListPage.isTaskInList(consoleTaskName)
        }
        assertTrue(!taskListPage.isTaskInList(consoleTaskName), "После удаления задача должна пропасть из списка")
    }

    @Test
    @Order(4)
    fun `create sql task`() {
        headerComponent.navigateToTaskList()

        taskListPage.navigateToCreateTask()

        addTaskPage.createSqlTask(
            sqlTaskName,
            "Test SQL task description",
            jsonSqlCriterionFilePath,
            sqlScriptFilePath
        )

        await().atMost(5, TimeUnit.SECONDS).until {
            addTaskPage.isTaskCreated()
        }

        assertTrue(addTaskPage.isTaskCreated(), "После создания SQL задачи должен произойти редирект на страницу задачи")
    }

    @Test
    @Order(5)
    fun `verify sql task in list and delete it`() {
        headerComponent.navigateToTaskList()

        await().atMost(5, TimeUnit.SECONDS).until {
            taskListPage.isTaskInList(sqlTaskName)
        }
        assertTrue(taskListPage.isTaskInList(sqlTaskName), "SQL задача должна быть в списке")

        taskListPage.openTask(sqlTaskName)

        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.getTaskName().contains(sqlTaskName)
        }

        taskPage.deleteTask()

        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.isTaskPageClosed()
        }
        assertTrue(taskPage.isTaskPageClosed(), "После удаления SQL задачи должен произойти редирект")

        headerComponent.navigateToTaskList()
        await().atMost(5, TimeUnit.SECONDS).until {
            !taskListPage.isTaskInList(sqlTaskName)
        }
        assertTrue(!taskListPage.isTaskInList(sqlTaskName), "После удаления SQL задача должна пропасть из списка")
    }

    @AfterAll
    fun tearDown() {
        driver.quit()
    }
}