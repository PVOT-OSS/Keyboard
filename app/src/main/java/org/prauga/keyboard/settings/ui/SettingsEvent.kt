/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import org.prauga.keyboard.toolbar.ToolbarKeys

internal sealed interface SettingsEvent {
    data object Refresh : SettingsEvent

    data class SetBoolean(val key: String, val value: Boolean) : SettingsEvent

    data class SetString(val key: String, val value: String) : SettingsEvent

    data class SetInt(val key: String, val value: Int) : SettingsEvent

    data class SetPercent(val key: String, val percent: Int) : SettingsEvent

    data class Reset(val key: String) : SettingsEvent

    data class SetTheme(val themeId: Int) : SettingsEvent

    data class SetToolbarKeys(val entries: List<ToolbarKeys.Entry>) : SettingsEvent

    data class BlockWord(val word: String) : SettingsEvent

    data class UnblockWord(val word: String) : SettingsEvent

    data class AddStyle(val locale: String, val layout: String) : SettingsEvent

    data class RemoveStyle(val prefSubtype: String) : SettingsEvent

    data class PreviewVibration(val milliseconds: Int) : SettingsEvent

    data class PreviewSound(val percent: Int) : SettingsEvent

    data object RemovePhoto : SettingsEvent

    data object ClearClipboard : SettingsEvent

    data object ClearLearnedWords : SettingsEvent

    data object ResetStats : SettingsEvent
}
