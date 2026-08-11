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
import ru.yarsu.pages.MyResultListPage
import ru.yarsu.pages.MySolutionListPage
import ru.yarsu.pages.SelectBundleTasksPage
import ru.yarsu.pages.SignInPage
import ru.yarsu.pages.SignUpPage
import ru.yarsu.pages.SolutionPage
import ru.yarsu.pages.TaskListPage
import ru.yarsu.pages.TaskPage
import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserSolutionTest : BaseTest() {
    private val signInPage by lazy { SignInPage(driver) }
    private val signUpPage by lazy { SignUpPage(driver) }
    private val headerComponent by lazy { HeaderComponent(driver) }
    private val bundleListPage by lazy { BundleListPage(driver) }
    private val addBundlePage by lazy { AddBundlePage(driver) }
    private val bundlePage by lazy { BundlePage(driver) }
    private val selectBundleTasksPage by lazy { SelectBundleTasksPage(driver) }
    private val changeBundleTasksOrderPage by lazy { ChangeBundleTasksOrderPage(driver) }
    private val addTaskPage by lazy { AddTaskPage(driver) }
    private val taskListPage by lazy { TaskListPage(driver) }
    private val taskPage by lazy { TaskPage(driver) }
    private val myResultListPage by lazy { MyResultListPage(driver) }
    private val mySolutionListPage by lazy { MySolutionListPage(driver) }
    private val solutionPage by lazy { SolutionPage(driver) }

    private val adminUsername = "admin"
    private val adminPassword = "pass"

    private val testUsername = "testuser_${System.currentTimeMillis()}"
    private val testName = "Тест"
    private val testSurname = "Пользователь"
    private val testPassword = "TestPassword123!"

    private val bundleName = "Test Bundle ${System.currentTimeMillis()}"
    private val consoleTaskName = "Console Task ${System.currentTimeMillis()}"
    private val sqlTaskName = "SQL Task ${System.currentTimeMillis()}"

    private val jsonConsoleCriterionFilePath = "src/jvmTest/resources/test-console-criterion.json"
    private val jsonSqlCriterionFilePath = "src/jvmTest/resources/test-sql-criterion.json"
    private val sqlScriptFilePath = "src/jvmTest/resources/test-script.sql"
    private val consoleSolutionPath = "src/jvmTest/resources/console-solution.py"
    private val sqlSolutionPath = "src/jvmTest/resources/sql-solution.sql"

    @Test
    @Order(1)
    fun `admin login and create tasks`() {
        signInPage.open()
        signInPage.login(adminUsername, adminPassword)
        await().atMost(5, TimeUnit.SECONDS).until { headerComponent.isHeaderDisplayed() }
        assertTrue(headerComponent.isHeaderDisplayed(), "Админ должен войти в систему")

        headerComponent.navigateToTaskList()
        addTaskPage.open()
        addTaskPage.createConsoleTask(consoleTaskName, "Описание консольной задачи", jsonConsoleCriterionFilePath)
        await().atMost(5, TimeUnit.SECONDS).until { addTaskPage.isTaskCreated() }
        assertTrue(addTaskPage.isTaskCreated(), "Консольная задача должна быть создана")

        addTaskPage.open()
        addTaskPage.createSqlTask(sqlTaskName, "Описание SQL задачи", jsonSqlCriterionFilePath, sqlScriptFilePath)
        await().atMost(5, TimeUnit.SECONDS).until { addTaskPage.isTaskCreated() }
        assertTrue(addTaskPage.isTaskCreated(), "SQL задача должна быть создана")
    }

    @Test
    @Order(2)
    fun `admin creates bundle with tasks`() {
        addBundlePage.open()
        addBundlePage.createBundle(bundleName)
        await().atMost(5, TimeUnit.SECONDS).until { addBundlePage.isBundleCreated() }
        assertTrue(addBundlePage.isBundleCreated(), "Набор должен быть создан")

        bundlePage.navigateToAddTasks()
        selectBundleTasksPage.selectTask(consoleTaskName)
        selectBundleTasksPage.selectTask(sqlTaskName)
        selectBundleTasksPage.saveSelection()
        changeBundleTasksOrderPage.saveOrder()
        await().atMost(5, TimeUnit.SECONDS).until { changeBundleTasksOrderPage.isOrderSaved() }
        assertTrue(changeBundleTasksOrderPage.isOrderSaved(), "Порядок задач должен быть сохранен")

        await().atMost(5, TimeUnit.SECONDS).until { bundlePage.isTaskInBundle(consoleTaskName) }
        await().atMost(5, TimeUnit.SECONDS).until { bundlePage.isTaskInBundle(sqlTaskName) }
        assertTrue(bundlePage.isTaskInBundle(consoleTaskName), "Консольная задача должна быть в наборе")
        assertTrue(bundlePage.isTaskInBundle(sqlTaskName), "SQL задача должна быть в наборе")

        headerComponent.signOut()
        await().atMost(5, TimeUnit.SECONDS).until { headerComponent.isUserSignOut() }
        assertTrue(headerComponent.isUserSignOut(), "Админ должен выйти из системы")
    }

    @Test
    @Order(3)
    fun `register new test user`() {
        signUpPage.open()
        signUpPage.register(testUsername, testName, testSurname, testPassword)
        await().atMost(5, TimeUnit.SECONDS).until { signUpPage.isRegistrationSuccessful() }
        assertTrue(signUpPage.isRegistrationSuccessful(), "Пользователь должен зарегистрироваться")
    }

    @Test
    @Order(4)
    fun `user opens bundle and submits console solution`() {
        bundleListPage.open()

        await().atMost(5, TimeUnit.SECONDS).until { bundleListPage.isBundleInList(bundleName) }
        bundleListPage.openBundle(bundleName)

        val taskElements = driver.findElements(org.openqa.selenium.By.cssSelector(".bundle-item"))
        val consoleTask = taskElements.find { it.text.contains(consoleTaskName) }
        if (consoleTask != null) {
            consoleTask.click()
            Thread.sleep(1000)
        } else {
            throw Exception("Консольная задача не найдена в наборе")
        }

        await().atMost(5, TimeUnit.SECONDS).until { taskPage.getTaskName().contains(consoleTaskName) }

        taskPage.uploadSolution(consoleSolutionPath)

        await().atMost(30, TimeUnit.SECONDS).until { solutionPage.isSolutionPageLoaded() }
        assertTrue(solutionPage.isSolutionPageLoaded(), "Страница решения должна загрузиться")
    }

    @Test
    @Order(5)
    fun `user checks my results for console task`() {
        headerComponent.navigateToMyResults()
        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isPageLoaded() }
        assertTrue(myResultListPage.isPageLoaded(), "Страница 'Мои решения' должна загрузиться")

        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isBundleNameVisible(bundleName) }
        assertTrue(myResultListPage.isBundleNameVisible(bundleName), "Название набора должно быть видно")

        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isTaskBlockVisible(consoleTaskName) }
        assertTrue(myResultListPage.isTaskBlockVisible(consoleTaskName), "Блок консольной задачи должен быть виден")

        myResultListPage.openTaskSolutions(consoleTaskName)

        await().atMost(5, TimeUnit.SECONDS).until { mySolutionListPage.isSolutionInList() }
        assertTrue(mySolutionListPage.isSolutionInList(), "Решение должно быть в списке")
        assertTrue(mySolutionListPage.getSolutionCount() >= 1, "Должно быть хотя бы одно решение")
    }

    @Test
    @Order(6)
    fun `user submits sql solution`() {
        headerComponent.navigateToMyResults()
        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isPageLoaded() }
        assertTrue(myResultListPage.isPageLoaded(), "Страница 'Мои решения' должна загрузиться")

        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isBundleNameVisible(bundleName) }
        assertTrue(myResultListPage.isBundleNameVisible(bundleName), "Название набора должно быть видно")

        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isTaskBlockVisible(sqlTaskName) }
        assertTrue(myResultListPage.isTaskBlockVisible(sqlTaskName), "Блок SQL задачи должен быть виден")

        myResultListPage.openTaskSolutions(sqlTaskName)

        await().atMost(5, TimeUnit.SECONDS).until { mySolutionListPage.isNoSolutionsMessage() }
        assertTrue(mySolutionListPage.isNoSolutionsMessage(), "Решений не должно быть в списке")

        mySolutionListPage.navigateToTask()
        await().atMost(5, TimeUnit.SECONDS).until {
            taskPage.getTaskName().contains(sqlTaskName)
        }

        taskPage.uploadSolution(sqlSolutionPath)

        await().atMost(30, TimeUnit.SECONDS).until { solutionPage.isSolutionPageLoaded() }
        assertTrue(solutionPage.isSolutionPageLoaded(), "Страница SQL решения должна загрузиться")
    }

    @Test
    @Order(7)
    fun `user verifies sql solution in my results`() {
        headerComponent.navigateToMyResults()
        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isPageLoaded() }

        await().atMost(5, TimeUnit.SECONDS).until { myResultListPage.isTaskBlockVisible(sqlTaskName) }
        myResultListPage.openTaskSolutions(sqlTaskName)

        await().atMost(5, TimeUnit.SECONDS).until { mySolutionListPage.isSolutionInList() }
        assertTrue(mySolutionListPage.isSolutionInList(), "SQL решение должно быть в списке")
        assertTrue(mySolutionListPage.getSolutionCount() >= 1, "Должно быть хотя бы одно SQL решение")
    }

    @Test
    @Order(8)
    fun `admin deletes bundle and tasks`() {
        headerComponent.signOut()
        await().atMost(5, TimeUnit.SECONDS).until { headerComponent.isUserSignOut() }

        signInPage.open()
        signInPage.login(adminUsername, adminPassword)
        await().atMost(5, TimeUnit.SECONDS).until { headerComponent.isHeaderDisplayed() }

        headerComponent.navigateToBundleList()
        bundleListPage.openBundle(bundleName)
        bundlePage.deleteBundle()
        await().atMost(5, TimeUnit.SECONDS).until { bundlePage.isBundlePageClosed() }
        assertTrue(bundlePage.isBundlePageClosed(), "Набор должен быть удален")

        headerComponent.navigateToBundleList()
        await().atMost(5, TimeUnit.SECONDS).until { !bundleListPage.isBundleInList(bundleName) }
        assertTrue(!bundleListPage.isBundleInList(bundleName), "Набор должен пропасть из списка")

        headerComponent.navigateToTaskList()
        taskListPage.openTask(consoleTaskName)
        await().atMost(5, TimeUnit.SECONDS).until { taskPage.getTaskName().contains(consoleTaskName) }
        taskPage.deleteTask()
        await().atMost(5, TimeUnit.SECONDS).until { taskPage.isTaskPageClosed() }

        headerComponent.navigateToTaskList()
        taskListPage.openTask(sqlTaskName)
        await().atMost(5, TimeUnit.SECONDS).until { taskPage.getTaskName().contains(sqlTaskName) }
        taskPage.deleteTask()
        await().atMost(5, TimeUnit.SECONDS).until { taskPage.isTaskPageClosed() }

        headerComponent.navigateToTaskList()
        await().atMost(5, TimeUnit.SECONDS).until { !taskListPage.isTaskInList(consoleTaskName) }
        await().atMost(5, TimeUnit.SECONDS).until { !taskListPage.isTaskInList(sqlTaskName) }
        assertTrue(!taskListPage.isTaskInList(consoleTaskName), "Консольная задача должна быть удалена")
        assertTrue(!taskListPage.isTaskInList(sqlTaskName), "SQL задача должна быть удалена")
    }
}