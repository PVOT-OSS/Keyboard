/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.compose.runtime.Composable
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import org.prauga.keyboard.R
import org.prauga.keyboard.gestures.KeyboardGestures
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.settings.data.SettingsSnapshot

@Composable
internal fun GestureScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
) {
    val glide = snapshot.boolean(Settings.PREF_GESTURE_INPUT, true)
    PvotSubScreen(screenTitle(SettingsRoute.GESTURE), onBack) {
        item {
            PvotListGroup {
                prefSwitch(
                    snapshot,
                    Settings.PREF_GESTURE_INPUT,
                    true,
                    R.string.gesture_input,
                    onEvent,
                    R.string.gesture_input_summary,
                )
                prefSwitch(
                    snapshot,
                    Settings.PREF_GESTURE_FLOATING_PREVIEW_TEXT,
                    true,
                    R.string.gesture_floating_preview_text,
                    onEvent,
                    R.string.gesture_floating_preview_text_summary,
                    enabled = glide,
                )
                prefSwitch(
                    snapshot,
                    Settings.PREF_GESTURE_PREVIEW_TRAIL,
                    true,
                    R.string.gesture_preview_trail,
                    onEvent,
                    enabled = glide,
                )
            }
        }
        item {
            PvotListGroup {
                prefSwitch(
                    snapshot,
                    KeyboardGestures.PREF_KEYBOARD_GESTURES,
                    false,
                    R.string.keyboard_gestures,
                    onEvent,
                    R.string.keyboard_gestures_summary,
                )
            }
        }
    }
}
