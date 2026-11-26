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
        return enabled
            .asSequence()
            .filter { it.language != primary.language && scriptOf(it) == script }
            .distinctBy { it.language }
            .take(MAX_SECONDARY)
            .toList()
    }
}
