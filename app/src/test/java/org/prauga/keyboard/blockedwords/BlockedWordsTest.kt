/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.blockedwords

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockedWordsTest {
    @Test
    fun normalizeTrimsAndLowercases() {
        assertEquals("hello", BlockedWords.normalize("  Hello "))
    }

    @Test
    fun isBlockedIgnoresCase() {
        val blocked = setOf("hello")
        assertTrue(BlockedWords.isBlocked(blocked, "Hello"))
        assertTrue(BlockedWords.isBlocked(blocked, "HELLO"))
        assertFalse(BlockedWords.isBlocked(blocked, "help"))
    }

    @Test
    fun emptySetBlocksNothing() {
        assertFalse(BlockedWords.isBlocked(emptySet(), "hello"))
    }
}
