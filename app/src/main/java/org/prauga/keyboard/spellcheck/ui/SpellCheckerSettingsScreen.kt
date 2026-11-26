/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.spellcheck.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.ui.SettingsEvent
import org.prauga.keyboard.settings.ui.settingsViewModel
import org.prauga.keyboard.settings.ui.switchRow
import org.prauga.keyboard.spellcheck.AndroidSpellCheckerService

@Composable
internal fun SpellCheckerSettingsScreen(onBack: () -> Unit) {
    val viewModel = settingsViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(SettingsEvent.Refresh)
        onPauseOrDispose {}
    }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.onEvent(SettingsEvent.SetBoolean(KEY, true))
        }
    val snapshot = (state as? UiState.Success)?.data ?: return
    PvotSubScreen(stringResource(R.string.android_spell_checker_settings), onBack) {
        item {
            PvotListGroup {
                switchRow(
                    title = R.string.use_contacts_for_spellchecking_option_title,
                    summary = R.string.use_contacts_for_spellchecking_option_summary,
                    checked = snapshot.contactsGranted && snapshot.boolean(KEY, true),
                    onCheckedChange = { on ->
                        if (on && !snapshot.contactsGranted) {
                            permission.launch(Manifest.permission.READ_CONTACTS)
                        } else {
                            viewModel.onEvent(SettingsEvent.SetBoolean(KEY, on))
                        }
                    },
                )
            }
        }
    }
}

private const val KEY = AndroidSpellCheckerService.PREF_USE_CONTACTS_KEY
