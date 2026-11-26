/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.prauga.keyboard.setup.SetupStatus
import org.prauga.keyboard.setup.SetupStep

internal data class SetupUiState(val welcome: Boolean, val step: SetupStep?)

internal class SetupWizardViewModel(private val status: SetupStatus) : ViewModel() {

    private val mutableState = MutableStateFlow(SetupUiState(welcome = false, step = null))

    val state: StateFlow<SetupUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val step = status.current()
            mutableState.value = SetupUiState(welcome = step == SetupStep.ENABLE, step = step)
        }
    }

    fun start() {
        mutableState.update { it.copy(welcome = false) }
    }

    fun backToWelcome(): Boolean {
        val current = mutableState.value
        if (current.welcome || current.step != SetupStep.ENABLE) return false
        mutableState.value = current.copy(welcome = true)
        return true
    }

    fun refresh() {
        viewModelScope.launch {
            val step = status.current()
            mutableState.update { it.copy(step = step) }
        }
    }
}
