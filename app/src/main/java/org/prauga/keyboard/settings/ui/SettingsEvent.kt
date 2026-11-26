/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import org.prauga.keyboard.toolbar.ToolbarKeys

internal sealed interface SettingsEvent {
    sealed interface Write : SettingsEvent

    sealed interface Action : SettingsEvent

    data object Refresh : Action

    data class SetBoolean(val key: String, val value: Boolean) : Write

    data class SetString(val key: String, val value: String) : Write

    data class SetInt(val key: String, val value: Int) : Write

    data class SetPercent(val key: String, val percent: Int) : Write

    data class Reset(val key: String) : Write

    data class SetTheme(val themeId: Int) : Write

    data class SetToolbarKeys(val entries: List<ToolbarKeys.Entry>) : Write

    data class BlockWord(val word: String) : Write

    data class UnblockWord(val word: String) : Write

    data class AddStyle(val locale: String, val layout: String) : Action

    data class RemoveStyle(val prefSubtype: String) : Action

    data class PreviewVibration(val milliseconds: Int) : Action

    data class PreviewSound(val percent: Int) : Action

    data object RemovePhoto : Action

    data object ClearClipboard : Action

    data object ClearLearnedWords : Action

    data object ResetStats : Action

    data class ShowLicense(val title: String, val path: String) : Action
}
