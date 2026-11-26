/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import org.prauga.keyboard.stats.TypingStatsCounter
import org.prauga.keyboard.toolbar.ToolbarKeys

internal data class SettingsSnapshot(
    val values: Map<String, Any?>,
    val languages: Languages?,
    val hasVibrator: Boolean,
    val showEmojiKeyOption: Boolean,
    val showVoiceKeyOption: Boolean,
    val showPopupOption: Boolean,
    val popupEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val soundEnabled: Boolean,
    val splitAvailable: Boolean,
    val themeId: Int,
    val photoSet: Boolean,
    val personalDictionaryAvailable: Boolean,
    val contactsGranted: Boolean,
    val popupDismissDelay: Int,
    val popupDefaultDelay: Int,
    val vibrationMs: Int,
    val soundPercent: Int,
    val soundDefaultPercent: Int,
    val longPressMs: Int,
    val heightPercent: Int,
    val stats: TypingStatsCounter,
    val toolbarKeys: List<ToolbarKeys.Entry>,
    val blockedWords: List<String>,
    val customStyles: List<CustomStyle>,
) {
    fun boolean(key: String, default: Boolean): Boolean = values[key] as? Boolean ?: default

    fun string(key: String, default: String): String = values[key] as? String ?: default
}

internal data class Languages(val imeId: String, val summary: String)

internal data class CustomStyle(val prefSubtype: String, val name: String)

internal data class Choice(val value: String, val label: String)

internal data class StyleChoices(val locales: List<Choice>, val layouts: List<Choice>)

internal sealed interface AddStyleResult {
    data class Added(val imeId: String) : AddStyleResult

    data class Exists(val name: String) : AddStyleResult
}
