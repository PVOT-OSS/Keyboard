/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.prauga.keyboard.setup.SetupStatusSource
import org.prauga.keyboard.setup.SetupStep

internal data class SetupUiState(val welcome: Boolean, val step: SetupStep?)

internal class SetupWizardViewModel(private val status: SetupStatusSource) : ViewModel() {

    private val mutableState = MutableStateFlow(SetupUiState(welcome = false, step = null))
    private val effectChannel = Channel<SetupEffect>(Channel.BUFFERED)
    private var polling: Job? = null

    val state: StateFlow<SetupUiState> = mutableState.asStateFlow()

    val effects: Flow<SetupEffect> = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val step = status.current()
            mutableState.value = SetupUiState(welcome = step == SetupStep.ENABLE, step = step)
        }
    }

    fun onEvent(event: SetupEvent) {
        when (event) {
            SetupEvent.Launched -> openSettingsIfDone()
            SetupEvent.Refresh -> refresh()
            SetupEvent.Start -> mutableState.update { it.copy(welcome = false) }
            SetupEvent.Back -> back()
            SetupEvent.EnableKeyboard -> enableKeyboard()
            SetupEvent.SwitchKeyboard -> effectChannel.trySend(SetupEffect.ShowImePicker)
            SetupEvent.ConfigureLanguages -> configureLanguages()
            SetupEvent.Finish -> effectChannel.trySend(SetupEffect.Close)
        }
    }

    private fun openSettingsIfDone() {
        viewModelScope.launch {
            if (status.current() == SetupStep.DONE) {
                effectChannel.send(SetupEffect.OpenSettings)
                effectChannel.send(SetupEffect.Close)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val step = status.current()
            mutableState.update { it.copy(step = step) }
        }
    }

    private fun back() {
        val current = mutableState.value
        if (!current.welcome && current.step == SetupStep.ENABLE) {
            mutableState.value = current.copy(welcome = true)
        } else {
            effectChannel.trySend(SetupEffect.Close)
        }
    }

    private fun enableKeyboard() {
        effectChannel.trySend(SetupEffect.OpenImeSettings)
        polling?.cancel()
        polling = viewModelScope.launch {
            repeat(MAX_POLLS) {
                delay(POLL_INTERVAL_MS)
                if (status.current() != SetupStep.ENABLE) {
                    effectChannel.send(SetupEffect.ReturnToWizard)
                    return@launch
                }
            }
        }
    }

    private fun configureLanguages() {
        viewModelScope.launch {
            status.imeId()?.let { effectChannel.send(SetupEffect.OpenLanguages(it)) }
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 200L
        const val MAX_POLLS = 1500
    }
}
