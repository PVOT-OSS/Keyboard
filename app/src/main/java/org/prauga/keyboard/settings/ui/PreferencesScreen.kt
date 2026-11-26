/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.settings.data.SettingsSnapshot

@Composable
internal fun PreferencesScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onOpen: (SettingsRoute) -> Unit,
    onBack: () -> Unit,
) {
    PvotSubScreen(screenTitle(SettingsRoute.PREFERENCES), onBack) {
        item { KeysGroup(snapshot, onEvent) }
        item { ToolbarGroup(snapshot, onEvent, onOpen) }
        item { FeedbackGroup(snapshot, onEvent) }
    }
}

@Composable
private fun KeysGroup(snapshot: SettingsSnapshot, onEvent: (SettingsEvent) -> Unit) {
    val numberRow = snapshot.boolean(Settings.PREF_SHOW_NUMBER_ROW, false)
    val settings =
        listOfNotNull(
            SwitchSetting(
                Settings.PREF_AUTO_CAP,
                true,
                R.string.auto_cap,
                R.string.auto_cap_summary,
            ),
            SwitchSetting(
                Settings.PREF_SHOW_NUMBER_ROW,
                false,
                R.string.number_row,
                R.string.number_row_summary,
            ),
            SwitchSetting(
                Settings.PREF_SHOW_NUMBER_ROW_PASSWORD,
                false,
                R.string.number_row_password,
                R.string.number_row_password_summary,
                enabled = !numberRow,
            ),
            SwitchSetting(
                Settings.PREF_ARROW_KEY_ROW,
                false,
                R.string.arrow_key_row,
                R.string.arrow_key_row_summary,
            ),
            SwitchSetting(
                    Settings.PREF_SHOW_EMOJI_KEY,
                    false,
                    R.string.show_emoji_key,
                    R.string.show_emoji_key_summary,
                )
                .takeIf { snapshot.showEmojiKeyOption },
            SwitchSetting(
                Settings.PREF_SHOW_LONGPRESS_HINTS,
                false,
                R.string.show_longpress_hints,
                R.string.show_longpress_hints_summary,
            ),
            SwitchSetting(
                Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD,
                true,
                R.string.use_double_space_period,
                R.string.use_double_space_period_summary,
            ),
            SwitchSetting(Settings.PREF_VOICE_INPUT_KEY, true, R.string.voice_input).takeIf {
                snapshot.showVoiceKeyOption
            },
        )
    PvotListGroup(title = stringResource(R.string.settings_group_keys)) {
        switches(snapshot, onEvent, settings)
    }
}

@Composable
private fun ToolbarGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onOpen: (SettingsRoute) -> Unit,
) {
    val toolbar = snapshot.boolean(Settings.PREF_SHOW_TOOLBAR, true)
    PvotListGroup(title = stringResource(R.string.settings_group_toolbar)) {
        prefSwitch(
            snapshot,
            Settings.PREF_SHOW_TOOLBAR,
            true,
            R.string.show_toolbar,
            onEvent,
            R.string.show_toolbar_summary,
        )
        linkRow(
            title = R.string.toolbar_keys,
            summaryRes = R.string.toolbar_keys_summary,
            enabled = toolbar,
            onClick = { onOpen(SettingsRoute.TOOLBAR_KEYS) },
        )
    }
}

@Composable
private fun FeedbackGroup(snapshot: SettingsSnapshot, onEvent: (SettingsEvent) -> Unit) {
    val settings =
        listOfNotNull(
            SwitchSetting(
                    Settings.PREF_VIBRATE_ON,
                    booleanResource(R.bool.config_default_vibration_enabled),
                    R.string.vibrate_on_keypress,
                )
                .takeIf { snapshot.hasVibrator },
            SwitchSetting(
                Settings.PREF_SOUND_ON,
                booleanResource(R.bool.config_default_sound_enabled),
                R.string.sound_on_keypress,
            ),
            SwitchSetting(
                    Settings.PREF_POPUP_ON,
                    booleanResource(R.bool.config_default_key_preview_popup),
                    R.string.popup_on_keypress,
                )
                .takeIf { snapshot.showPopupOption },
        )
    PvotListGroup(title = stringResource(R.string.settings_group_feedback)) {
        switches(snapshot, onEvent, settings)
    }
}
