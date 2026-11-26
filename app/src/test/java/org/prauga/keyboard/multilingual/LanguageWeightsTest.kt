/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.multilingual

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageWeightsTest {
    private val dutch = Locale("nl")
    private val german = Locale("de")

    @Test
    fun unusedLanguagesStartLow() {
        assertEquals(0.85f, LanguageWeights().weight(dutch), 0.0001f)
    }

    @Test
    fun typingInALanguageRaisesItsWeight() {
        val weights = LanguageWeights()
        repeat(20) { weights.record(listOf(dutch)) }
        assertTrue(weights.weight(dutch) > 0.97f)
        assertEquals(0.85f, weights.weight(german), 0.0001f)
    }

    @Test
    fun weightsFadeWhenALanguageIsNoLongerUsed() {
        val weights = LanguageWeights()
        repeat(20) { weights.record(listOf(dutch)) }
        val peak = weights.weight(dutch)
        repeat(100) { weights.record(emptyList()) }
        assertTrue(weights.weight(dutch) < peak - 0.05f)
    }
}
