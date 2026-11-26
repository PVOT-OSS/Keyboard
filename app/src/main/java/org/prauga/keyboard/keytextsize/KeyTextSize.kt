/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.keytextsize

import android.content.SharedPreferences

object KeyTextSize {
    const val PREF_KEY_TEXT_SIZE = "pref_key_text_size"

    @JvmStatic
    fun read(prefs: SharedPreferences): Float = scaleOf(prefs.getString(PREF_KEY_TEXT_SIZE, null))

    private const val SMALL = 0.85f
    private const val DEFAULT = 1.0f
    private const val LARGE = 1.15f
    private const val LARGEST = 1.3f

    internal fun scaleOf(value: String?): Float =
        when (value) {
            "small" -> SMALL
            "large" -> LARGE
            "largest" -> LARGEST
            else -> DEFAULT
        }
}
