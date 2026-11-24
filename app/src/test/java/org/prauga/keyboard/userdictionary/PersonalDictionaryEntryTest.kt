/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalDictionaryEntryTest {
    @Test
    fun roundTripKeepsAllFields() {
        val parsed = PersonalDictionaryEntry.fromLine(
            PersonalDictionaryEntry("Pvot", 200, "en_US", "pv").toLine()
        )
        assertEquals("Pvot", parsed?.word)
        assertEquals(200, parsed?.frequency)
        assertEquals("en_US", parsed?.locale)
        assertEquals("pv", parsed?.shortcut)
    }

    @Test
    fun wordOnlyLineUsesDefaults() {
        val parsed = PersonalDictionaryEntry.fromLine("hello")
        assertEquals(PersonalDictionaryEntry.DEFAULT_FREQUENCY, parsed?.frequency)
        assertEquals("", parsed?.locale)
        assertNull(parsed?.shortcut)
    }

    @Test
    fun frequencyIsClamped() {
        assertEquals(255, PersonalDictionaryEntry.fromLine("a\t999")?.frequency)
        assertEquals(1, PersonalDictionaryEntry.fromLine("a\t-5")?.frequency)
    }

    @Test
    fun invalidLinesAreSkipped() {
        assertNull(PersonalDictionaryEntry.fromLine(""))
        assertNull(PersonalDictionaryEntry.fromLine("# comment"))
        assertNull(PersonalDictionaryEntry.fromLine("\t100"))
        assertNull(PersonalDictionaryEntry.fromLine("word\tnotanumber"))
        assertNull(PersonalDictionaryEntry.fromLine("x".repeat(49)))
    }
}
