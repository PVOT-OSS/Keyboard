/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.stats

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.CustomPreferenceFragment
import java.text.NumberFormat

class TypingStatsFragment : CustomPreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val context = requireContext()
        val stats = TypingStats.snapshot(context)
        val screen = preferenceScreen
        screen.removeAll()
        addRow(R.string.typing_stats_words, stats.words)
        addRow(R.string.typing_stats_glided, stats.glidedWords)
        addRow(R.string.typing_stats_autocorrections, stats.autocorrections)
        addRow(R.string.typing_stats_keystrokes_saved, stats.keystrokesSaved)
        screen.addPreference(Preference(context).apply {
            isPersistent = false
            setTitle(R.string.typing_stats_reset)
            setSummary(R.string.typing_stats_privacy)
            setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.typing_stats_reset)
                    .setPositiveButton(R.string.typing_stats_reset_confirm) { _, _ ->
                        TypingStats.reset(context)
                        refresh()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
        })
    }

    private fun addRow(@StringRes title: Int, value: Long) {
        preferenceScreen.addPreference(Preference(requireContext()).apply {
            isPersistent = false
            isSelectable = false
            setTitle(title)
            summary = NumberFormat.getIntegerInstance().format(value)
        })
    }
}
