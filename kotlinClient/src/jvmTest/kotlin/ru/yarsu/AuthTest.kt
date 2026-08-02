package ru.yarsu

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import ru.yarsu.pages.Header
import ru.yarsu.pages.SignInPage
import ru.yarsu.pages.SignUpPage
import java.time.Duration
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthTest {
    private lateinit var driver: WebDriver
    private lateinit var signUpPage: SignUpPage
    private lateinit var signInPage: SignInPage
    private lateinit var header: Header

    private val testUsername = "testuser_${System.currentTimeMillis()}"
    private val testName = "Иван"
    private val testSurname = "Иванов"
    private val testPassword = "TestPassword123!"

    @BeforeAll
    fun setUp() {
        val options = ChromeOptions().apply {
            addArguments("--headless", "--window-size=1920,1080", "--disable-gpu", "--no-sandbox")
        }
        driver = ChromeDriver(options)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3))

        signUpPage = SignUpPage(driver)
        signInPage = SignInPage(driver)
        header = Header(driver)
    }

    @Test
    fun `test registration and login`() {
        signUpPage.open()

        signUpPage.register(testUsername, testName, testSurname, testPassword)

        await().atMost(5, TimeUnit.SECONDS).until {
            signUpPage.isRegistrationSuccessful()
        }

        assertTrue(signUpPage.isRegistrationSuccessful(), "После регистрации пользователь должен быть авторизован")

        header.signOut()

        await().atMost(5, TimeUnit.SECONDS).until {
            header.isUserSignOut()
        }

        assertFalse(header.isUserSignIn(), "После выхода пользователь не должен быть авторизован")

        signInPage.login(testUsername, testPassword)

        await().atMost(5, TimeUnit.SECONDS).until {
            signInPage.isLoginSuccessful()
        }

        assertTrue(header.isUserSignIn(), "После входа пользователь должен быть авторизован")

        header.signOut()

        await().atMost(5, TimeUnit.SECONDS).until {
            header.isUserSignOut()
        }

        assertFalse(header.isUserSignIn(), "После выхода пользователь не должен быть авторизован")
    }

    @AfterAll
    fun tearDown() {
        driver.quit()
    }
}