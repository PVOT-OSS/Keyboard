/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.SettingsActivity
import org.prauga.keyboard.setup.ui.SetupActions
import org.prauga.keyboard.setup.ui.SetupWizardScreen
import org.prauga.keyboard.setup.ui.SetupWizardViewModel

class SetupWizardActivity : PvotBaseActivity() {

    private val status by lazy { SetupStatus(this) }
    private val viewModel: SetupWizardViewModel by viewModels {
        viewModelFactory { initializer { SetupWizardViewModel(status) } }
    }
    private var polling: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firstLaunch = savedInstanceState == null
        val appName = getString(R.string.english_ime_name)
        val actions =
            SetupActions(
                onStart = viewModel::start,
                onEnable = ::openImeSettings,
                onSwitch = ::showPicker,
                onLanguages = ::openLanguages,
                onFinish = ::finish,
            )
        setPvotContent {
            PvotAppTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(state.step) {
                    if (firstLaunch && state.step == SetupStep.DONE && !state.welcome) {
                        openSettings()
                    }
                }
                BackHandler(enabled = state.step == SetupStep.ENABLE && !state.welcome) {
                    viewModel.backToWelcome()
                }
                SetupWizardScreen(state, appName, actions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) viewModel.refresh()
    }

    private fun openImeSettings() {
        startActivity(
            Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addCategory(Intent.CATEGORY_DEFAULT)
        )
        polling?.cancel()
        polling = lifecycleScope.launch {
            repeat(MAX_POLLS) {
                delay(POLL_INTERVAL_MS)
                if (status.current() != SetupStep.ENABLE) {
                    returnToWizard()
                    return@launch
                }
            }
        }
    }

    private fun returnToWizard() {
        startActivity(
            Intent(this, SetupWizardActivity::class.java)
                .setFlags(
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
        )
    }

    private fun showPicker() {
        getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
    }

    private fun openLanguages() {
        lifecycleScope.launch {
            val imeId = status.imeId() ?: return@launch
            startActivity(
                Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .putExtra(Settings.EXTRA_INPUT_METHOD_ID, imeId)
            )
        }
    }

    private fun openSettings() {
        startActivity(
            Intent(this, SettingsActivity::class.java)
                .setFlags(
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
                .putExtra(
                    SettingsActivity.EXTRA_ENTRY_KEY,
                    SettingsActivity.EXTRA_ENTRY_VALUE_APP_ICON,
                )
        )
        finish()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 200L
        const val MAX_POLLS = 1500
    }
}
