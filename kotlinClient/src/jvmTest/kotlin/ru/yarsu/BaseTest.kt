package ru.yarsu

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {
    @BeforeAll
    fun setUp() {
        TestConfig.setUp()
    }

    @AfterAll
    fun tearDown() {
        TestConfig.tearDown()
    }
}