/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListGroupScope
import com.prauga.pvot.designsystem.components.list.PvotPreferenceRow
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.settings.data.SettingsSnapshot

@Composable
internal fun AdvancedScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
    onBack: () -> Unit,
) {
    PvotSubScreen(screenTitle(SettingsRoute.ADVANCED), onBack) {
        item { TimingGroup(snapshot, onEvent, dialogs) }
        item { KeyboardGroup(snapshot, onEvent, dialogs) }
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_other)) {
                prefSwitch(
                    snapshot,
                    Settings.PREF_SHOW_SETUP_WIZARD_ICON,
                    false,
                    R.string.show_setup_wizard_icon,
                    onEvent,
                    R.string.show_setup_wizard_icon_summary,
                )
            }
        }
    }
}

@Composable
private fun TimingGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
) {
    val res = LocalContext.current.resources
    val ms = { value: Int -> milliseconds(res, value) }
    val vibration =
        SliderSetting(
            Settings.PREF_VIBRATION_DURATION_SETTINGS,
            R.string.prefs_keypress_vibration_duration_settings,
            snapshot.vibrationMs,
            0..integerResource(R.integer.config_max_vibration_duration),
            ms,
            { SettingsEvent.SetInt(Settings.PREF_VIBRATION_DURATION_SETTINGS, it) },
            preview = { SettingsEvent.PreviewVibration(it) },
            enabled = snapshot.vibrationEnabled,
        )
    val sound =
        SliderSetting(
            Settings.PREF_KEYPRESS_SOUND_VOLUME,
            R.string.prefs_keypress_sound_volume_settings,
            if (snapshot.soundPercent < 0) snapshot.soundDefaultPercent else snapshot.soundPercent,
            0..PERCENT_MAX,
            { percentOrDefault(res, it) },
            { SettingsEvent.SetPercent(Settings.PREF_KEYPRESS_SOUND_VOLUME, it) },
            preview = { SettingsEvent.PreviewSound(it) },
            enabled = snapshot.soundEnabled,
            label = percentOrDefault(res, snapshot.soundPercent),
        )
    val longPress =
        SliderSetting(
            Settings.PREF_KEY_LONGPRESS_TIMEOUT,
            R.string.prefs_key_longpress_timeout_settings,
            snapshot.longPressMs,
            integerResource(R.integer.config_min_longpress_timeout)..integerResource(
                    R.integer.config_max_longpress_timeout
                ),
            ms,
            { SettingsEvent.SetInt(Settings.PREF_KEY_LONGPRESS_TIMEOUT, it) },
            step = integerResource(R.integer.config_longpress_timeout_step),
        )
    val titles = listOf(vibration, sound, longPress).map { stringResource(it.title) }
    PvotListGroup(title = stringResource(R.string.settings_group_timing)) {
        if (snapshot.showPopupOption) popupDelayRow(snapshot, onEvent, dialogs)
        if (snapshot.hasVibrator) sliderRow(vibration, titles[0], dialogs, onEvent)
        sliderRow(sound, titles[1], dialogs, onEvent)
        sliderRow(longPress, titles[2], dialogs, onEvent)
    }
}

private fun PvotListGroupScope.popupDelayRow(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
) = item {
    val entries =
        listOf(
            stringResource(R.string.key_preview_popup_dismiss_no_delay),
            stringResource(R.string.key_preview_popup_dismiss_default_delay),
        )
    val values = listOf(0, snapshot.popupDefaultDelay)
    val title = stringResource(R.string.key_preview_popup_dismiss_delay)
    val index = values.indexOf(snapshot.popupDismissDelay)
    PvotPreferenceRow(
        title = title,
        value = entries.getOrNull(index),
        enabled = snapshot.popupEnabled,
        onClick = {
            dialogs.choice =
                ChoiceRequest(title, entries, index) {
                    onEvent(
                        SettingsEvent.SetString(
                            Settings.PREF_KEY_PREVIEW_POPUP_DISMISS_DELAY,
                            values[it].toString(),
                        )
                    )
                }
        },
    )
}

@Composable
private fun KeyboardGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
) {
    val height =
        SliderSetting(
            Settings.PREF_KEYBOARD_HEIGHT_SCALE,
            R.string.prefs_keyboard_height_scale,
            snapshot.heightPercent,
            HEIGHT_MIN..HEIGHT_MAX,
            { "$it%" },
            { SettingsEvent.SetPercent(Settings.PREF_KEYBOARD_HEIGHT_SCALE, it) },
        )
    val heightTitle = stringResource(height.title)
    val settings =
        listOf(
            SwitchSetting(
                Settings.PREF_SPACE_TRACKPAD,
                true,
                R.string.space_trackpad,
                R.string.space_trackpad_summary,
            ),
            SwitchSetting(
                Settings.PREF_BACKSPACE_TRACKPAD,
                true,
                R.string.backspace_trackpad,
                R.string.backspace_trackpad_summary,
            ),
            SwitchSetting(
                Settings.PREF_SWIPE_DOWN_FOR_DIGITS,
                false,
                R.string.swipe_down_for_digits,
                R.string.swipe_down_for_digits_summary,
            ),
            SwitchSetting(
                Settings.PREF_ENABLE_EMOJI_ALT_PHYSICAL_KEY,
                true,
                R.string.prefs_enable_emoji_alt_physical_key,
                R.string.prefs_enable_emoji_alt_physical_key_summary,
            ),
        )
    PvotListGroup(title = stringResource(R.string.settings_group_keyboard)) {
        sliderRow(height, heightTitle, dialogs, onEvent)
        switches(snapshot, onEvent, settings)
    }
}

private fun milliseconds(res: Resources, value: Int): String =
    if (value < 0) res.getString(R.string.settings_system_default)
    else res.getString(R.string.abbreviation_unit_milliseconds, value.toString())

private fun percentOrDefault(res: Resources, value: Int): String =
    if (value < 0) res.getString(R.string.settings_system_default) else value.toString()

private const val PERCENT_MAX = 100
private const val HEIGHT_MIN = 50
private const val HEIGHT_MAX = 120
