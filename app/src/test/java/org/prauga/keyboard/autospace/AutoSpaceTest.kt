/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.autospace

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoSpaceTest {
    @Test
    fun spaceAfterPunctuationFollowingAWord() {
        assertTrue(AutoSpace.shouldInsertSpace("Hello."))
        assertTrue(AutoSpace.shouldInsertSpace("yes,"))
        assertTrue(AutoSpace.shouldInsertSpace("ok?"))
    }

    @Test
    fun noSpaceForNumbersAbbreviationsOrExistingSpace() {
        assertFalse(AutoSpace.shouldInsertSpace("3."))
        assertFalse(AutoSpace.shouldInsertSpace("e.g."))
        assertFalse(AutoSpace.shouldInsertSpace(" e."))
        assertFalse(AutoSpace.shouldInsertSpace("word. "))
        assertFalse(AutoSpace.shouldInsertSpace("word"))
        assertFalse(AutoSpace.shouldInsertSpace(null))
    }
}
