package ru.yarsu

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.yarsu.TestConfig.driver
import ru.yarsu.pages.HeaderComponent
import ru.yarsu.pages.SignInPage
import ru.yarsu.pages.SignUpPage
import java.util.concurrent.TimeUnit

class AuthTest : BaseTest() {
    private val signUpPage by lazy { SignUpPage(driver) }
    private val signInPage by lazy { SignInPage(driver) }
    private val header by lazy { HeaderComponent(driver) }

    private val testUsername = "testuser_${System.currentTimeMillis()}"
    private val testName = "Иван"
    private val testSurname = "Иванов"
    private val testPassword = "TestPassword123!"

    @Test
    fun `test registration and login`() {
        signInPage.open()

        signInPage.switchToSignUpPage()

        signUpPage.register(testUsername, testName, testSurname, testPassword)

        await().atMost(5, TimeUnit.SECONDS).until {
            signUpPage.isRegistrationSuccessful()
        }

        assertTrue(signUpPage.isRegistrationSuccessful(), "После регистрации пользователь должен быть авторизован")

        header.signOut()

        await().atMost(5, TimeUnit.SECONDS).until {
            header.isUserSignOut()
        }

        assertFalse(header.isHeaderDisplayed(), "После выхода пользователь не должен быть авторизован")

        signInPage.login(testUsername, testPassword)

        await().atMost(5, TimeUnit.SECONDS).until {
            signInPage.isLoginSuccessful()
        }

        assertTrue(header.isHeaderDisplayed(), "После входа пользователь должен быть авторизован")

        header.signOut()

        await().atMost(5, TimeUnit.SECONDS).until {
            header.isUserSignOut()
        }

        assertFalse(header.isHeaderDisplayed(), "После выхода пользователь не должен быть авторизован")
    }
}