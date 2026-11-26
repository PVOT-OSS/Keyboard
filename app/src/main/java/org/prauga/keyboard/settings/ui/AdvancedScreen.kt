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
    val res = LocalContext.current.resources
    val maxVibration = integerResource(R.integer.config_max_vibration_duration)
    val minLongPress = integerResource(R.integer.config_min_longpress_timeout)
    val maxLongPress = integerResource(R.integer.config_max_longpress_timeout)
    val longPressStep = integerResource(R.integer.config_longpress_timeout_step)
    val delayEntries =
        listOf(
            stringResource(R.string.key_preview_popup_dismiss_no_delay),
            stringResource(R.string.key_preview_popup_dismiss_default_delay),
        )
    val delayValues = listOf(0, snapshot.popupDefaultDelay)
    val delayTitle = stringResource(R.string.key_preview_popup_dismiss_delay)
    val vibrationTitle = stringResource(R.string.prefs_keypress_vibration_duration_settings)
    val soundTitle = stringResource(R.string.prefs_keypress_sound_volume_settings)
    val longPressTitle = stringResource(R.string.prefs_key_longpress_timeout_settings)
    val heightTitle = stringResource(R.string.prefs_keyboard_height_scale)
    PvotSubScreen(screenTitle(SettingsRoute.ADVANCED), onBack) {
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_timing)) {
                if (snapshot.showPopupOption) {
                    val index = delayValues.indexOf(snapshot.popupDismissDelay)
                    linkRow(
                        title = R.string.key_preview_popup_dismiss_delay,
                        value = delayEntries.getOrNull(index),
                        enabled = snapshot.popupEnabled,
                        onClick = {
                            dialogs.choice =
                                ChoiceRequest(delayTitle, delayEntries, index) {
                                    onEvent(
                                        SettingsEvent.SetString(
                                            Settings.PREF_KEY_PREVIEW_POPUP_DISMISS_DELAY,
                                            delayValues[it].toString(),
                                        )
                                    )
                                }
                        },
                    )
                }
                if (snapshot.hasVibrator) {
                    linkRow(
                        title = R.string.prefs_keypress_vibration_duration_settings,
                        value = milliseconds(res, snapshot.vibrationMs),
                        enabled = snapshot.vibrationEnabled,
                        onClick = {
                            dialogs.slider =
                                SliderRequest(
                                    title = vibrationTitle,
                                    value = snapshot.vibrationMs,
                                    range = 0..maxVibration,
                                    step = 1,
                                    format = { milliseconds(res, it) },
                                    onPreview = { onEvent(SettingsEvent.PreviewVibration(it)) },
                                    onSave = {
                                        onEvent(
                                            SettingsEvent.SetInt(
                                                Settings.PREF_VIBRATION_DURATION_SETTINGS, it
                                            )
                                        )
                                    },
                                    onDefault = {
                                        onEvent(
                                            SettingsEvent.Reset(Settings.PREF_VIBRATION_DURATION_SETTINGS)
                                        )
                                    },
                                )
                        },
                    )
                }
                linkRow(
                    title = R.string.prefs_keypress_sound_volume_settings,
                    value = percentOrDefault(res, snapshot.soundPercent),
                    enabled = snapshot.soundEnabled,
                    onClick = {
                        dialogs.slider =
                            SliderRequest(
                                title = soundTitle,
                                value =
                                    if (snapshot.soundPercent < 0) snapshot.soundDefaultPercent
                                    else snapshot.soundPercent,
                                range = 0..PERCENT_MAX,
                                step = 1,
                                format = { it.toString() },
                                onPreview = { onEvent(SettingsEvent.PreviewSound(it)) },
                                onSave = {
                                    onEvent(SettingsEvent.SetPercent(Settings.PREF_KEYPRESS_SOUND_VOLUME, it))
                                },
                                onDefault = {
                                    onEvent(SettingsEvent.Reset(Settings.PREF_KEYPRESS_SOUND_VOLUME))
                                },
                            )
                    },
                )
                linkRow(
                    title = R.string.prefs_key_longpress_timeout_settings,
                    value = milliseconds(res, snapshot.longPressMs),
                    onClick = {
                        dialogs.slider =
                            SliderRequest(
                                title = longPressTitle,
                                value = snapshot.longPressMs,
                                range = minLongPress..maxLongPress,
                                step = longPressStep,
                                format = { milliseconds(res, it) },
                                onPreview = {},
                                onSave = {
                                    onEvent(SettingsEvent.SetInt(Settings.PREF_KEY_LONGPRESS_TIMEOUT, it))
                                },
                                onDefault = {
                                    onEvent(SettingsEvent.Reset(Settings.PREF_KEY_LONGPRESS_TIMEOUT))
                                },
                            )
                    },
                )
            }
        }
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_keyboard)) {
                linkRow(
                    title = R.string.prefs_keyboard_height_scale,
                    value = "${snapshot.heightPercent}%",
                    onClick = {
                        dialogs.slider =
                            SliderRequest(
                                title = heightTitle,
                                value = snapshot.heightPercent,
                                range = HEIGHT_MIN..HEIGHT_MAX,
                                step = 1,
                                format = { "$it%" },
                                onPreview = {},
                                onSave = {
                                    onEvent(SettingsEvent.SetPercent(Settings.PREF_KEYBOARD_HEIGHT_SCALE, it))
                                },
                                onDefault = {
                                    onEvent(SettingsEvent.Reset(Settings.PREF_KEYBOARD_HEIGHT_SCALE))
                                },
                            )
                    },
                )
                prefSwitch(snapshot, Settings.PREF_SPACE_TRACKPAD, true, R.string.space_trackpad,
                    onEvent, R.string.space_trackpad_summary)
                prefSwitch(snapshot, Settings.PREF_BACKSPACE_TRACKPAD, true,
                    R.string.backspace_trackpad, onEvent, R.string.backspace_trackpad_summary)
                prefSwitch(snapshot, Settings.PREF_SWIPE_DOWN_FOR_DIGITS, false,
                    R.string.swipe_down_for_digits, onEvent, R.string.swipe_down_for_digits_summary)
                prefSwitch(snapshot, Settings.PREF_ENABLE_EMOJI_ALT_PHYSICAL_KEY, true,
                    R.string.prefs_enable_emoji_alt_physical_key, onEvent,
                    R.string.prefs_enable_emoji_alt_physical_key_summary)
            }
        }
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_other)) {
                prefSwitch(snapshot, Settings.PREF_SHOW_SETUP_WIZARD_ICON, false,
                    R.string.show_setup_wizard_icon, onEvent, R.string.show_setup_wizard_icon_summary)
            }
        }
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
