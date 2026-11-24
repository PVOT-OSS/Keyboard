/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PersonalDictionaryEntryTest {
    @Test
    public void roundTripKeepsAllFields() {
        final PersonalDictionaryEntry entry =
                new PersonalDictionaryEntry("Pvot", 200, "en_US", "pv");
        final PersonalDictionaryEntry parsed = PersonalDictionaryEntry.fromLine(entry.toLine());
        assertNotNull(parsed);
        assertEquals("Pvot", parsed.mWord);
        assertEquals(200, parsed.mFrequency);
        assertEquals("en_US", parsed.mLocale);
        assertEquals("pv", parsed.mShortcut);
    }

    @Test
    public void wordOnlyLineUsesDefaults() {
        final PersonalDictionaryEntry parsed = PersonalDictionaryEntry.fromLine("hello");
        assertNotNull(parsed);
        assertEquals(PersonalDictionaryEntry.DEFAULT_FREQUENCY, parsed.mFrequency);
        assertEquals("", parsed.mLocale);
        assertNull(parsed.mShortcut);
    }

    @Test
    public void frequencyIsClamped() {
        assertEquals(255, PersonalDictionaryEntry.fromLine("a\t999").mFrequency);
        assertEquals(1, PersonalDictionaryEntry.fromLine("a\t-5").mFrequency);
    }

    @Test
    public void invalidLinesAreSkipped() {
        assertNull(PersonalDictionaryEntry.fromLine(""));
        assertNull(PersonalDictionaryEntry.fromLine("# comment"));
        assertNull(PersonalDictionaryEntry.fromLine("\t100"));
        assertNull(PersonalDictionaryEntry.fromLine("word\tnotanumber"));
        assertNull(PersonalDictionaryEntry.fromLine("x".repeat(49)));
    }
}
