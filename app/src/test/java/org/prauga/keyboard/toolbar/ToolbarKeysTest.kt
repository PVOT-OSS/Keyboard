/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.toolbar

import org.junit.Assert.assertEquals
import org.junit.Test

class ToolbarKeysTest {
    @Test
    fun defaultShowsAShortToolbar() {
        val entries = ToolbarKeys.parse(null)
        assertEquals(
            listOf(
                "clipboard",
                "editing",
                "modes",
                "one_handed",
                "resize",
                "float",
                "emoji",
                "settings",
                "incognito",
            ),
            entries.map { it.id },
        )
        assertEquals(
            listOf("one_handed", "resize", "float", "incognito"),
            entries.filter { !it.shown }.map { it.id },
        )
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
        assertEquals(9, ids.size)
        assertEquals(ids.distinct(), ids)
    }
}
