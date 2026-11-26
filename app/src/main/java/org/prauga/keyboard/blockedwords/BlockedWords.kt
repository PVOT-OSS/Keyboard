/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.blockedwords

import android.content.SharedPreferences
import java.util.Locale

object BlockedWords {
    const val PREF_BLOCKED_WORDS = "pref_blocked_words"

    @JvmStatic
    fun read(prefs: SharedPreferences): Set<String> =
        prefs.getStringSet(PREF_BLOCKED_WORDS, null)?.toSortedSet() ?: emptySet()

    @JvmStatic
    fun add(prefs: SharedPreferences, word: String) {
        val normalized = normalize(word)
        if (normalized.isEmpty()) {
            return
        }
        val words = read(prefs).toMutableSet()
        if (words.add(normalized)) {
            prefs.edit().putStringSet(PREF_BLOCKED_WORDS, words).apply()
        }
    }

    @JvmStatic
    fun remove(prefs: SharedPreferences, word: String) {
        val words = read(prefs).toMutableSet()
        if (words.remove(normalize(word))) {
            prefs.edit().putStringSet(PREF_BLOCKED_WORDS, words).apply()
        }
    }

    @JvmStatic
    fun isBlocked(blockedWords: Set<String>, word: String): Boolean =
        blockedWords.isNotEmpty() && normalize(word) in blockedWords

    @JvmStatic fun normalize(word: String): String = word.trim().lowercase(Locale.ROOT)
}
