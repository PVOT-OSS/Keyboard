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

    internal fun scaleOf(value: String?): Float =
        when (value) {
            "small" -> 0.85f
            "large" -> 1.15f
            "largest" -> 1.3f
            else -> 1.0f
        }
}
