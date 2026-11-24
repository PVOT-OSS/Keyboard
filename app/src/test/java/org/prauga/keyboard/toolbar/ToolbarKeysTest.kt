/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.toolbar

import org.junit.Assert.assertEquals
import org.junit.Test

class ToolbarKeysTest {
    @Test
    fun defaultShowsEveryKeyInTheStandardOrder() {
        assertEquals(
            listOf("clipboard", "editing", "one_handed", "resize", "float", "emoji", "settings"),
            ToolbarKeys.parse(null).map { it.id },
        )
        assertEquals(true, ToolbarKeys.parse(null).all { it.shown })
    }

    @Test
    fun orderAndHiddenKeysRoundTrip() {
        val entries = ToolbarKeys.parse("emoji,!clipboard,settings")
        assertEquals(ToolbarKeys.Entry("emoji", true), entries[0])
        assertEquals(ToolbarKeys.Entry("clipboard", false), entries[1])
        assertEquals(entries, ToolbarKeys.parse(ToolbarKeys.serialize(entries)))
    }

    @Test
    fun unknownAndDuplicateKeysAreDroppedAndMissingOnesAppended() {
        val ids = ToolbarKeys.parse("emoji,gif,emoji").map { it.id }
        assertEquals("emoji", ids[0])
        assertEquals(7, ids.size)
        assertEquals(ids.distinct(), ids)
    }
}
