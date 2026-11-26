/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.AddStyleResult
import org.prauga.keyboard.settings.data.CustomStyle
import org.prauga.keyboard.settings.data.InputStylesRepository
import org.prauga.keyboard.settings.data.SettingsActions
import org.prauga.keyboard.settings.data.SettingsRepository
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.settings.data.SettingsWriter
import org.prauga.keyboard.settings.data.StyleChoices
import org.prauga.keyboard.stats.TypingStatsCounter
import org.prauga.keyboard.toolbar.ToolbarKeys

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeSettingsRepository()
    private val writer = FakeSettingsWriter()
    private val styles = FakeInputStylesRepository()
    private val actions = FakeSettingsActions()
    private val testSubject by lazy { SettingsViewModel(repository, writer, styles, actions) }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsTheSnapshotAndReloadsOnRefresh() =
        runTest(dispatcher) {
            testSubject.state.test {
                assertThat(awaitItem()).isEqualTo(UiState.Loading)
                assertThat(awaitItem()).isInstanceOf(UiState.Success::class)
                testSubject.onEvent(SettingsEvent.Refresh)
                advanceUntilIdle()
                assertThat(repository.loads).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun preferenceWritesReachTheWriter() {
        testSubject.onEvent(SettingsEvent.SetBoolean("auto_cap", false))
        testSubject.onEvent(SettingsEvent.SetTheme(THEME_ID))
        testSubject.onEvent(SettingsEvent.BlockWord("word"))

        assertThat(writer.writes).containsExactly("auto_cap=false", "theme=$THEME_ID", "block=word")
    }

    @Test
    fun clearingTheClipboardReportsIt() =
        runTest(dispatcher) {
            testSubject.effects.test {
                testSubject.onEvent(SettingsEvent.ClearClipboard)
                assertThat(awaitItem())
                    .isEqualTo(SettingsEffect.Message(R.string.clear_clipboard_history_done))
                assertThat(actions.clipboardCleared).isEqualTo(true)
            }
        }

    @Test
    fun addingAStyleThatExistsSaysSo() =
        runTest(dispatcher) {
            styles.result = AddStyleResult.Exists(STYLE_NAME)

            testSubject.effects.test {
                testSubject.onEvent(SettingsEvent.AddStyle("de", "qwerty"))
                assertThat(awaitItem())
                    .isEqualTo(
                        SettingsEffect.Message(
                            R.string.custom_input_style_already_exists,
                            STYLE_NAME,
                        )
                    )
            }
        }

    @Test
    fun addingANewStyleOffersToEnableIt() =
        runTest(dispatcher) {
            styles.result = AddStyleResult.Added(IME_ID)

            testSubject.effects.test {
                testSubject.onEvent(SettingsEvent.AddStyle("de", "qwerty"))
                assertThat(awaitItem()).isEqualTo(SettingsEffect.OfferEnableStyle(IME_ID))
            }
        }

    @Test
    fun licencesOpenAsDocuments() =
        runTest(dispatcher) {
            testSubject.effects.test {
                testSubject.onEvent(SettingsEvent.ShowLicense("Glisse", "licenses/MIT-Glisse.txt"))
                assertThat(awaitItem())
                    .isEqualTo(
                        SettingsEffect.ShowDocument("Glisse", "text of licenses/MIT-Glisse.txt")
                    )
            }
        }

    private class FakeSettingsRepository : SettingsRepository {
        var loads = 0

        override val changes: Flow<Unit> = MutableStateFlow(Unit)

        override suspend fun load(): SettingsSnapshot {
            loads++
            return snapshot()
        }
    }

    private class FakeSettingsWriter : SettingsWriter {
        val writes = mutableListOf<String>()

        override fun putBoolean(key: String, value: Boolean) {
            writes += "$key=$value"
        }

        override fun putString(key: String, value: String) {
            writes += "$key=$value"
        }

        override fun putInt(key: String, value: Int) {
            writes += "$key=$value"
        }

        override fun putPercent(key: String, percent: Int) {
            writes += "$key=$percent%"
        }

        override fun reset(key: String) {
            writes += "reset=$key"
        }

        override fun setTheme(themeId: Int) {
            writes += "theme=$themeId"
        }

        override fun setToolbarKeys(entries: List<ToolbarKeys.Entry>) {
            writes += "toolbar=${entries.size}"
        }

        override fun blockWord(word: String) {
            writes += "block=$word"
        }

        override fun unblockWord(word: String) {
            writes += "unblock=$word"
        }
    }

    private class FakeInputStylesRepository : InputStylesRepository {
        var result: AddStyleResult = AddStyleResult.Added(IME_ID)

        override suspend fun styleChoices() = StyleChoices(emptyList(), emptyList())

        override suspend fun addStyle(locale: String, layout: String) = result

        override suspend fun removeStyle(prefSubtype: String) = Unit

        override fun customStyles(): List<CustomStyle> = emptyList()
    }

    private class FakeSettingsActions : SettingsActions {
        var clipboardCleared = false

        override suspend fun removePhoto() = Unit

        override fun clearClipboard() {
            clipboardCleared = true
        }

        override suspend fun clearLearnedWords() = Unit

        override suspend fun resetStats() = Unit

        override suspend fun readDocument(path: String) = "text of $path"

        override fun playKeySound(percent: Int) = Unit

        override fun vibrate(milliseconds: Int) = Unit
    }

    private companion object {
        const val THEME_ID = 9
        const val IME_ID = "org.prauga.keyboard/.LatinIME"
        const val STYLE_NAME = "German (QWERTY)"

        fun snapshot() =
            SettingsSnapshot(
                values = emptyMap(),
                languages = null,
                hasVibrator = true,
                showEmojiKeyOption = true,
                showVoiceKeyOption = true,
                showPopupOption = true,
                popupEnabled = true,
                vibrationEnabled = true,
                soundEnabled = false,
                splitAvailable = true,
                themeId = THEME_ID,
                photoSet = false,
                personalDictionaryAvailable = true,
                contactsGranted = false,
                popupDismissDelay = 100,
                popupDefaultDelay = 100,
                vibrationMs = -1,
                soundPercent = -1,
                soundDefaultPercent = 50,
                longPressMs = 300,
                heightPercent = 100,
                stats = TypingStatsCounter(),
                toolbarKeys = emptyList(),
                blockedWords = emptyList(),
                customStyles = emptyList(),
            )
    }
}
