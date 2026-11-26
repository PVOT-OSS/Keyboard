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
    val vibrationDefault = booleanResource(R.bool.config_default_vibration_enabled)
    val soundDefault = booleanResource(R.bool.config_default_sound_enabled)
    val popupDefault = booleanResource(R.bool.config_default_key_preview_popup)
    val numberRow = snapshot.boolean(Settings.PREF_SHOW_NUMBER_ROW, false)
    val toolbar = snapshot.boolean(Settings.PREF_SHOW_TOOLBAR, true)
    PvotSubScreen(screenTitle(SettingsRoute.PREFERENCES), onBack) {
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_keys)) {
                prefSwitch(snapshot, Settings.PREF_AUTO_CAP, true, R.string.auto_cap, onEvent,
                    R.string.auto_cap_summary)
                prefSwitch(snapshot, Settings.PREF_SHOW_NUMBER_ROW, false, R.string.number_row,
                    onEvent, R.string.number_row_summary)
                prefSwitch(snapshot, Settings.PREF_SHOW_NUMBER_ROW_PASSWORD, false,
                    R.string.number_row_password, onEvent, R.string.number_row_password_summary,
                    enabled = !numberRow)
                prefSwitch(snapshot, Settings.PREF_ARROW_KEY_ROW, false, R.string.arrow_key_row,
                    onEvent, R.string.arrow_key_row_summary)
                if (snapshot.showEmojiKeyOption) {
                    prefSwitch(snapshot, Settings.PREF_SHOW_EMOJI_KEY, false,
                        R.string.show_emoji_key, onEvent, R.string.show_emoji_key_summary)
                }
                prefSwitch(snapshot, Settings.PREF_SHOW_LONGPRESS_HINTS, false,
                    R.string.show_longpress_hints, onEvent, R.string.show_longpress_hints_summary)
                prefSwitch(snapshot, Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD, true,
                    R.string.use_double_space_period, onEvent,
                    R.string.use_double_space_period_summary)
                if (snapshot.showVoiceKeyOption) {
                    prefSwitch(snapshot, Settings.PREF_VOICE_INPUT_KEY, true, R.string.voice_input,
                        onEvent)
                }
            }
        }
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_toolbar)) {
                prefSwitch(snapshot, Settings.PREF_SHOW_TOOLBAR, true, R.string.show_toolbar,
                    onEvent, R.string.show_toolbar_summary)
                linkRow(
                    title = R.string.toolbar_keys,
                    summaryRes = R.string.toolbar_keys_summary,
                    enabled = toolbar,
                    onClick = { onOpen(SettingsRoute.TOOLBAR_KEYS) },
                )
            }
        }
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_feedback)) {
                if (snapshot.hasVibrator) {
                    prefSwitch(snapshot, Settings.PREF_VIBRATE_ON, vibrationDefault,
                        R.string.vibrate_on_keypress, onEvent)
                }
                prefSwitch(snapshot, Settings.PREF_SOUND_ON, soundDefault,
                    R.string.sound_on_keypress, onEvent)
                if (snapshot.showPopupOption) {
                    prefSwitch(snapshot, Settings.PREF_POPUP_ON, popupDefault,
                        R.string.popup_on_keypress, onEvent)
                }
            }
        }
    }
}
