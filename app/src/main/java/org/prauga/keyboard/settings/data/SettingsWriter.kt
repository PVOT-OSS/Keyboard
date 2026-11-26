/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import org.prauga.keyboard.toolbar.ToolbarKeys

internal interface SettingsWriter {
    fun putBoolean(key: String, value: Boolean)

    fun putString(key: String, value: String)

    fun putInt(key: String, value: Int)

    fun putPercent(key: String, percent: Int)

    fun reset(key: String)

    fun setTheme(themeId: Int)

    fun setToolbarKeys(entries: List<ToolbarKeys.Entry>)

    fun blockWord(word: String)

    fun unblockWord(word: String)
}
