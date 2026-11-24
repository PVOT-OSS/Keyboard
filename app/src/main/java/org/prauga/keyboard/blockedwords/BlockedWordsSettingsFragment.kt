/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.blockedwords

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.CustomPreferenceFragment

class BlockedWordsSettingsFragment : CustomPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefs = requireNotNull(preferenceManager.sharedPreferences) { "No shared preferences" }
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
        refresh()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
        refresh()
    }

    override fun onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == BlockedWords.PREF_BLOCKED_WORDS) {
            refresh()
        }
    }

    private fun refresh() {
        val context = requireContext()
        val screen = preferenceScreen
        screen.removeAll()
        screen.addPreference(Preference(context).apply {
            isPersistent = false
            setTitle(R.string.blocked_words_add)
            setIcon(R.drawable.ic_add_circle)
            setOnPreferenceClickListener {
                showAddDialog()
                true
            }
        })
        val words = BlockedWords.read(prefs)
        if (words.isEmpty()) {
            screen.addPreference(Preference(context).apply {
                isPersistent = false
                isSelectable = false
                setSummary(R.string.blocked_words_empty)
            })
            return
        }
        for (word in words) {
            screen.addPreference(Preference(context).apply {
                isPersistent = false
                title = word
                setOnPreferenceClickListener {
                    showUnblockDialog(word)
                    true
                }
            })
        }
    }

    private fun showAddDialog() {
        val view = layoutInflater.inflate(R.layout.blocked_word_input, null)
        val input = view.findViewById<EditText>(R.id.blocked_word)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.blocked_words_add)
            .setView(view)
            .setPositiveButton(R.string.blocked_words_block) { _, _ ->
                BlockedWords.add(prefs, input.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showUnblockDialog(word: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.blocked_words_unblock_title, word))
            .setPositiveButton(R.string.blocked_words_unblock) { _, _ ->
                BlockedWords.remove(prefs, word)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
