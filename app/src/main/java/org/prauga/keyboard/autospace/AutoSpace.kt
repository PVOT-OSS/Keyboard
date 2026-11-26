/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.autospace

object AutoSpace {
    const val PREF_AUTO_SPACE_AFTER_PUNCTUATION = "pref_auto_space_after_punctuation"
    private const val PUNCTUATION = ".,!?;:"

    @JvmStatic
    fun shouldInsertSpace(textBeforeCursor: CharSequence?): Boolean {
        val text = textBeforeCursor ?: return false
        val n = text.length
        return n >= 3 && text[n - 1] in PUNCTUATION &&
            Character.isLetter(text[n - 2]) && Character.isLetter(text[n - 3])
    }
}
