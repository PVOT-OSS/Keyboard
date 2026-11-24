/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.toolbar

import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.StringRes
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.CustomPreferenceFragment

class ToolbarKeysSettingsFragment : CustomPreferenceFragment() {
    private lateinit var prefs: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefs = requireNotNull(preferenceManager.sharedPreferences) { "No shared preferences" }
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
        refresh()
    }

    private fun refresh() {
        val screen = preferenceScreen
        screen.removeAll()
        val entries = ToolbarKeys.readEntries(prefs)
        entries.forEachIndexed { index, entry ->
            screen.addPreference(ToolbarKeyPreference(requireContext()).apply {
                setTitle(titleOf(entry.id))
                shown = entry.shown
                canMoveUp = index > 0
                canMoveDown = index < entries.size - 1
                onToggle = { update(entries.toMutableList().also { it[index] = entry.copy(shown = !entry.shown) }) }
                onMoveUp = { update(entries.toMutableList().also { it.add(index - 1, it.removeAt(index)) }) }
                onMoveDown = { update(entries.toMutableList().also { it.add(index + 1, it.removeAt(index)) }) }
            })
        }
    }

    private fun update(entries: List<ToolbarKeys.Entry>) {
        ToolbarKeys.write(prefs, entries)
        refresh()
    }

    @StringRes
    private fun titleOf(id: String): Int = when (id) {
        ToolbarKeys.CLIPBOARD -> R.string.clipboard_history
        ToolbarKeys.EDITING -> R.string.text_editing
        ToolbarKeys.ONE_HANDED -> R.string.one_handed_mode
        ToolbarKeys.RESIZE -> R.string.keyboard_resize
        ToolbarKeys.FLOAT -> R.string.spoken_description_float_keyboard
        ToolbarKeys.EMOJI -> R.string.spoken_description_emoji
        else -> R.string.spoken_description_settings
    }
}
