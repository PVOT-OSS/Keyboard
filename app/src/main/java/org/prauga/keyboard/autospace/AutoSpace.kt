/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.autospace

object AutoSpace {
    const val PREF_AUTO_SPACE_AFTER_PUNCTUATION = "pref_auto_space_after_punctuation"
    private const val PUNCTUATION = ".,!?;:"
    private const val MIN_LETTERS_BEFORE = 2

    @JvmStatic
    fun shouldInsertSpace(textBeforeCursor: CharSequence?): Boolean {
        val text = textBeforeCursor ?: return false
        val n = text.length
        return n > MIN_LETTERS_BEFORE &&
            text[n - 1] in PUNCTUATION &&
            (2..MIN_LETTERS_BEFORE + 1).all { Character.isLetter(text[n - it]) }
    }
}
