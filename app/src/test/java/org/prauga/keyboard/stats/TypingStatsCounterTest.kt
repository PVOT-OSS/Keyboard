/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.keyboard.stats.TypingStatsCounter.Commit

class TypingStatsCounterTest {
    @Test
    fun countsWordsCorrectionsAndSavedKeystrokes() {
        val stats = TypingStatsCounter()
            .record("teh", "the", Commit.DECIDED, glided = false)
            .record("hel", "hello", Commit.PICKED, glided = false)
            .record("", "keyboard", Commit.PICKED, glided = true)
            .record("ok", "ok", Commit.DECIDED, glided = false)
        assertEquals(TypingStatsCounter(words = 4, glidedWords = 1, autocorrections = 1, keystrokesSaved = 10), stats)
    }

    @Test
    fun emptyCommitsAreIgnored() {
        assertEquals(TypingStatsCounter(), TypingStatsCounter().record("", "", Commit.TYPED, glided = false))
    }
}
