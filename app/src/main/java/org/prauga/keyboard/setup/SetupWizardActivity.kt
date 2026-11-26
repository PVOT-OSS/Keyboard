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
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme
import kotlinx.coroutines.launch
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.SettingsActivity
import org.prauga.keyboard.setup.ui.SetupEffect
import org.prauga.keyboard.setup.ui.SetupEvent
import org.prauga.keyboard.setup.ui.SetupWizardScreen
import org.prauga.keyboard.setup.ui.SetupWizardViewModel

class SetupWizardActivity : PvotBaseActivity() {

    private val viewModel: SetupWizardViewModel by viewModels {
        viewModelFactory { initializer { SetupWizardViewModel(SetupStatus(applicationContext)) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { viewModel.effects.collect(::handle) }
        if (savedInstanceState == null) {
            viewModel.onEvent(SetupEvent.Launched)
        }
        val appName = getString(R.string.english_ime_name)
        setPvotContent {
            PvotAppTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                BackHandler(enabled = state.step == SetupStep.ENABLE && !state.welcome) {
                    viewModel.onEvent(SetupEvent.Back)
                }
                SetupWizardScreen(state, appName, viewModel::onEvent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onEvent(SetupEvent.Refresh)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) viewModel.onEvent(SetupEvent.Refresh)
    }

    private fun handle(effect: SetupEffect) {
        when (effect) {
            SetupEffect.OpenImeSettings ->
                startActivity(
                    Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                )
            SetupEffect.ShowImePicker ->
                getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
            is SetupEffect.OpenLanguages ->
                startActivity(
                    Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .putExtra(Settings.EXTRA_INPUT_METHOD_ID, effect.imeId)
                )
            SetupEffect.OpenSettings ->
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                        .setFlags(
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                        .putExtra(
                            SettingsActivity.EXTRA_ENTRY_KEY,
                            SettingsActivity.EXTRA_ENTRY_VALUE_APP_ICON,
                        )
                )
            SetupEffect.ReturnToWizard ->
                startActivity(
                    Intent(this, SetupWizardActivity::class.java)
                        .setFlags(
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                )
            SetupEffect.Close -> finish()
        }
    }
}
