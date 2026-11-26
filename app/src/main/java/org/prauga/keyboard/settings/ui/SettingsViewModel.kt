/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.AddStyleResult
import org.prauga.keyboard.settings.data.InputStylesRepository
import org.prauga.keyboard.settings.data.SettingsActions
import org.prauga.keyboard.settings.data.SettingsRepository
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.settings.data.SettingsWriter
import org.prauga.keyboard.settings.data.StyleChoices

internal class SettingsViewModel(
    private val repository: SettingsRepository,
    private val writer: SettingsWriter,
    private val styles: InputStylesRepository,
    private val actions: SettingsActions,
) : ViewModel() {

    private val refreshes = MutableStateFlow(0)
    private val effectChannel = Channel<SettingsEffect>(Channel.BUFFERED)

    val effects: Flow<SettingsEffect> = effectChannel.receiveAsFlow()

    val state: StateFlow<UiState<SettingsSnapshot>> =
        combine(repository.changes, refreshes) { _, _ -> }
            .map { UiState.Success(repository.load()) as UiState<SettingsSnapshot> }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val choices = MutableStateFlow<StyleChoices?>(null)

    val styleChoices: StateFlow<StyleChoices?> = choices

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.Write -> write(event)
            is SettingsEvent.Action -> run(event)
        }
    }

    private fun write(event: SettingsEvent.Write) {
        when (event) {
            is SettingsEvent.SetBoolean -> writer.putBoolean(event.key, event.value)
            is SettingsEvent.SetString -> writer.putString(event.key, event.value)
            is SettingsEvent.SetInt -> writer.putInt(event.key, event.value)
            is SettingsEvent.SetPercent -> writer.putPercent(event.key, event.percent)
            is SettingsEvent.Reset -> writer.reset(event.key)
            is SettingsEvent.SetTheme -> writer.setTheme(event.themeId)
            is SettingsEvent.SetToolbarKeys -> writer.setToolbarKeys(event.entries)
            is SettingsEvent.BlockWord -> writer.blockWord(event.word)
            is SettingsEvent.UnblockWord -> writer.unblockWord(event.word)
        }
    }

    private fun run(event: SettingsEvent.Action) {
        when (event) {
            SettingsEvent.Refresh -> refreshes.value++
            is SettingsEvent.AddStyle -> addStyle(event.locale, event.layout)
            is SettingsEvent.RemoveStyle -> refreshAfter { styles.removeStyle(event.prefSubtype) }
            is SettingsEvent.PreviewVibration -> actions.vibrate(event.milliseconds)
            is SettingsEvent.PreviewSound -> actions.playKeySound(event.percent)
            SettingsEvent.RemovePhoto -> viewModelScope.launch { actions.removePhoto() }
            SettingsEvent.ClearClipboard -> {
                actions.clearClipboard()
                effectChannel.trySend(SettingsEffect.Message(R.string.clear_clipboard_history_done))
            }
            SettingsEvent.ClearLearnedWords ->
                viewModelScope.launch {
                    actions.clearLearnedWords()
                    effectChannel.send(SettingsEffect.Message(R.string.clear_learned_words_done))
                }
            SettingsEvent.ResetStats -> refreshAfter { actions.resetStats() }
            is SettingsEvent.ShowLicense ->
                viewModelScope.launch {
                    val text = actions.readDocument(event.path)
                    effectChannel.send(SettingsEffect.ShowDocument(event.title, text))
                }
        }
    }

    fun loadStyleChoices() {
        if (choices.value != null) return
        viewModelScope.launch { choices.value = styles.styleChoices() }
    }

    private fun addStyle(locale: String, layout: String) {
        viewModelScope.launch {
            when (val result = styles.addStyle(locale, layout)) {
                is AddStyleResult.Added ->
                    effectChannel.send(SettingsEffect.OfferEnableStyle(result.imeId))
                is AddStyleResult.Exists ->
                    effectChannel.send(
                        SettingsEffect.Message(
                            R.string.custom_input_style_already_exists,
                            result.name,
                        )
                    )
            }
            refreshes.value++
        }
    }

    private fun refreshAfter(action: suspend () -> Unit) {
        viewModelScope.launch {
            action()
            refreshes.value++
        }
    }

    private companion object {
        const val STOP_TIMEOUT = 5_000L
    }
}
