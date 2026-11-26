/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.stats

data class TypingStatsCounter(
    val words: Long = 0,
    val glidedWords: Long = 0,
    val autocorrections: Long = 0,
    val keystrokesSaved: Long = 0,
) {
    enum class Commit {
        TYPED,
        PICKED,
        DECIDED,
    }

    fun record(
        typedWord: String,
        chosenWord: String,
        commit: Commit,
        glided: Boolean,
    ): TypingStatsCounter {
        if (chosenWord.isEmpty()) {
            return this
        }
        val saved =
            when {
                glided -> chosenWord.length
                commit == Commit.PICKED -> (chosenWord.length - typedWord.length).coerceAtLeast(0)
                else -> 0
            }
        val corrected = !glided && commit == Commit.DECIDED && chosenWord != typedWord
        return copy(
            words = words + 1,
            glidedWords = glidedWords + if (glided) 1 else 0,
            autocorrections = autocorrections + if (corrected) 1 else 0,
            keystrokesSaved = keystrokesSaved + saved,
        )
    }
}
