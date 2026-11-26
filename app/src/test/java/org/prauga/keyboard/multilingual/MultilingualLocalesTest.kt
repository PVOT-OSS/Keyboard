/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.multilingual

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class MultilingualLocalesTest {
    private val scripts = mapOf("en" to 1, "nl" to 1, "de" to 1, "fr" to 1, "es" to 1, "ru" to 2)
    private val scriptOf = { locale: Locale -> scripts.getValue(locale.language) }

    @Test
    fun keepsOtherLanguagesWithTheSameScript() {
        val enabled = listOf(Locale("en", "IN"), Locale("ru"), Locale("nl"))
        assertEquals(
            listOf(Locale("nl")),
            MultilingualLocales.secondaryLocales(Locale("en", "IN"), enabled, scriptOf),
        )
    }

    @Test
    fun skipsOtherVariantsOfThePrimaryLanguageAndDuplicates() {
        val enabled = listOf(Locale("en", "US"), Locale("de"), Locale("de", "CH"))
        assertEquals(
            listOf(Locale("de")),
            MultilingualLocales.secondaryLocales(Locale("en", "IN"), enabled, scriptOf),
        )
    }

    @Test
    fun usesAtMostThreeSecondaryLanguages() {
        val enabled = listOf(Locale("nl"), Locale("de"), Locale("fr"), Locale("es"))
        assertEquals(3, MultilingualLocales.secondaryLocales(Locale("en"), enabled, scriptOf).size)
    }
}
