/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.toolbar

import android.content.SharedPreferences

object ToolbarKeys {
    const val PREF_TOOLBAR_KEYS = "pref_toolbar_keys"

    const val CLIPBOARD = "clipboard"
    const val EDITING = "editing"
    const val ONE_HANDED = "one_handed"
    const val RESIZE = "resize"
    const val FLOAT = "float"
    const val EMOJI = "emoji"
    const val SETTINGS = "settings"

    private const val HIDDEN_PREFIX = "!"
    private const val SEPARATOR = ","
    private val ALL = listOf(CLIPBOARD, EDITING, ONE_HANDED, RESIZE, FLOAT, EMOJI, SETTINGS)

    data class Entry(val id: String, val shown: Boolean)

    @JvmStatic
    fun read(prefs: SharedPreferences): List<String> =
        readEntries(prefs).filter { it.shown }.map { it.id }

    fun readEntries(prefs: SharedPreferences): List<Entry> =
        parse(prefs.getString(PREF_TOOLBAR_KEYS, null))

    fun write(prefs: SharedPreferences, entries: List<Entry>) {
        prefs.edit().putString(PREF_TOOLBAR_KEYS, serialize(entries)).apply()
    }

    internal fun parse(value: String?): List<Entry> {
        val entries = ArrayList<Entry>()
        for (token in value.orEmpty().split(SEPARATOR)) {
            val shown = !token.startsWith(HIDDEN_PREFIX)
            val id = token.removePrefix(HIDDEN_PREFIX)
            if (id in ALL && entries.none { it.id == id }) {
                entries.add(Entry(id, shown))
            }
        }
        for (id in ALL) {
            if (entries.none { it.id == id }) {
                entries.add(Entry(id, true))
            }
        }
        return entries
    }

    internal fun serialize(entries: List<Entry>): String =
        entries.joinToString(SEPARATOR) { if (it.shown) it.id else HIDDEN_PREFIX + it.id }
}
