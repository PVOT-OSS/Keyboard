/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.stats

import android.content.Context
import android.content.SharedPreferences
import org.prauga.keyboard.LastComposedWord

object TypingStats {
    private const val FILE = "typing_stats"
    private const val KEY_WORDS = "words"
    private const val KEY_GLIDED = "glided_words"
    private const val KEY_AUTOCORRECTIONS = "autocorrections"
    private const val KEY_SAVED = "keystrokes_saved"
    private const val FLUSH_EVERY = 20

    private var prefs: SharedPreferences? = null
    private var counter = TypingStatsCounter()
    private var unsaved = 0

    @JvmStatic
    @Synchronized
    fun snapshot(context: Context): TypingStatsCounter {
        load(context)
        return counter
    }

    @JvmStatic
    @Synchronized
    fun record(context: Context, typedWord: String, chosenWord: String, commitType: Int, glided: Boolean) {
        load(context)
        val commit = when (commitType) {
            LastComposedWord.COMMIT_TYPE_MANUAL_PICK -> TypingStatsCounter.Commit.PICKED
            LastComposedWord.COMMIT_TYPE_DECIDED_WORD -> TypingStatsCounter.Commit.DECIDED
            else -> TypingStatsCounter.Commit.TYPED
        }
        counter = counter.record(typedWord, chosenWord, commit, glided)
        if (++unsaved >= FLUSH_EVERY) {
            flush()
        }
    }

    @JvmStatic
    @Synchronized
    fun flush() {
        val store = prefs ?: return
        if (unsaved == 0) {
            return
        }
        unsaved = 0
        store.edit()
            .putLong(KEY_WORDS, counter.words)
            .putLong(KEY_GLIDED, counter.glidedWords)
            .putLong(KEY_AUTOCORRECTIONS, counter.autocorrections)
            .putLong(KEY_SAVED, counter.keystrokesSaved)
            .apply()
    }

    @JvmStatic
    @Synchronized
    fun reset(context: Context) {
        load(context)
        counter = TypingStatsCounter()
        unsaved = 0
        prefs?.edit()?.clear()?.apply()
    }

    private fun load(context: Context) {
        if (prefs != null) {
            return
        }
        val store = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        prefs = store
        counter = TypingStatsCounter(
            store.getLong(KEY_WORDS, 0),
            store.getLong(KEY_GLIDED, 0),
            store.getLong(KEY_AUTOCORRECTIONS, 0),
            store.getLong(KEY_SAVED, 0),
        )
    }
}
