/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.multilingual

import java.util.Locale

object MultilingualLocales {
    const val PREF_MULTILINGUAL_TYPING = "pref_multilingual_typing"
    private const val MAX_SECONDARY = 3

    @JvmStatic
    fun secondaryLocales(
        primary: Locale,
        enabled: List<Locale>,
        scriptOf: (Locale) -> Int,
    ): List<Locale> {
        val script = scriptOf(primary)
        val result = ArrayList<Locale>()
        for (locale in enabled) {
            if (locale.language == primary.language || result.any { it.language == locale.language }) {
                continue
            }
            if (scriptOf(locale) == script) {
                result.add(locale)
            }
            if (result.size == MAX_SECONDARY) {
                break
            }
        }
        return result
    }
}
