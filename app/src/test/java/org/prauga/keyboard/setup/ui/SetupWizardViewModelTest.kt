/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup.ui

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.prauga.keyboard.setup.SetupStatusSource
import org.prauga.keyboard.setup.SetupStep

@OptIn(ExperimentalCoroutinesApi::class)
class SetupWizardViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val status = FakeSetupStatus()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsOnTheWelcomeScreenWhenTheKeyboardIsNotEnabled() =
        runTest(dispatcher) {
            val testSubject = createLoaded(SetupStep.ENABLE)

            assertThat(testSubject.state.value)
                .isEqualTo(SetupUiState(welcome = true, SetupStep.ENABLE))
        }

    @Test
    fun startsOnTheStepsWhenTheKeyboardIsAlreadyEnabled() =
        runTest(dispatcher) {
            val testSubject = createLoaded(SetupStep.SWITCH)

            assertThat(testSubject.state.value.welcome).isFalse()
        }

    @Test
    fun startLeavesTheWelcomeScreenAndBackReturnsToIt() =
        runTest(dispatcher) {
            val testSubject = createLoaded(SetupStep.ENABLE)

            testSubject.onEvent(SetupEvent.Start)
            assertThat(testSubject.state.value.welcome).isFalse()
            testSubject.onEvent(SetupEvent.Back)
            assertThat(testSubject.state.value.welcome).isTrue()
        }

    @Test
    fun backOnTheWelcomeScreenCloses() =
        runTest(dispatcher) {
            val testSubject = createLoaded(SetupStep.ENABLE)

            testSubject.effects.test {
                testSubject.onEvent(SetupEvent.Back)
                assertThat(awaitItem()).isEqualTo(SetupEffect.Close)
            }
        }

    @Test
    fun enablingOpensSettingsAndComesBackOnceTheKeyboardIsEnabled() =
        runTest(dispatcher) {
            val testSubject = createLoaded(SetupStep.ENABLE)

            testSubject.effects.test {
                testSubject.onEvent(SetupEvent.EnableKeyboard)
                assertThat(awaitItem()).isEqualTo(SetupEffect.OpenImeSettings)
                advanceTimeBy(POLL_TIME)
                expectNoEvents()
                status.step = SetupStep.SWITCH
                advanceTimeBy(POLL_TIME)
                assertThat(awaitItem()).isEqualTo(SetupEffect.ReturnToWizard)
            }
        }

    @Test
    fun launchingWhenAlreadySetUpOpensSettingsAndCloses() =
        runTest(dispatcher) {
            val testSubject = createLoaded(SetupStep.DONE)

            testSubject.effects.test {
                testSubject.onEvent(SetupEvent.Launched)
                assertThat(awaitItem()).isEqualTo(SetupEffect.OpenSettings)
                assertThat(awaitItem()).isEqualTo(SetupEffect.Close)
            }
        }

    @Test
    fun configuringLanguagesOpensThisKeyboardsLanguages() =
        runTest(dispatcher) {
            val testSubject = createLoaded(SetupStep.DONE)

            testSubject.effects.test {
                testSubject.onEvent(SetupEvent.ConfigureLanguages)
                assertThat(awaitItem()).isEqualTo(SetupEffect.OpenLanguages(IME_ID))
            }
        }

    private fun TestScope.createLoaded(step: SetupStep): SetupWizardViewModel {
        status.step = step
        return SetupWizardViewModel(status).also { advanceUntilIdle() }
    }

    private class FakeSetupStatus : SetupStatusSource {
        var step = SetupStep.ENABLE

        override suspend fun current(): SetupStep = step

        override suspend fun imeId(): String = IME_ID
    }

    private companion object {
        const val IME_ID = "org.prauga.keyboard/.LatinIME"
        const val POLL_TIME = 250L
    }
}
