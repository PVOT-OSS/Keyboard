/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringReader

class EmojiKeywordsTest {
    private val index = EmojiKeywords.Index(
        EmojiKeywords.parse(
            StringReader(
                "# header\n" +
                    "cat\t$CAT $CAT_FACE\n" +
                    "catch\t$BALL\n" +
                    "face\t$GRIN $CAT_FACE\n" +
                    "pizza\t$PIZZA\n" +
                    "broken\n" +
                    "empty\t\n"
            )
        )
    )

    @Test
    fun parseKeepsEmojiInOrderAndSkipsBadLines() {
        assertEquals(
            mapOf(
                "cat" to listOf(CAT, CAT_FACE),
                "catch" to listOf(BALL),
                "face" to listOf(GRIN, CAT_FACE),
                "pizza" to listOf(PIZZA),
            ),
            index.keywords,
        )
    }

    @Test
    fun exactMatchesComeBeforePrefixMatches() {
        assertEquals(listOf(CAT, CAT_FACE, BALL), index.search("cat"))
    }

    @Test
    fun searchIgnoresCaseAndExtraSpaces() {
        assertEquals(listOf(PIZZA), index.search("  PIZ "))
    }

    @Test
    fun allWordsMustMatch() {
        assertEquals(listOf(CAT_FACE), index.search("cat face"))
    }

    @Test
    fun emptyOrUnknownQueryFindsNothing() {
        assertTrue(index.search(" ").isEmpty())
        assertTrue(index.search("zebra").isEmpty())
    }

    private companion object {
        const val CAT = "🐈"
        const val CAT_FACE = "🐱"
        const val BALL = "⚾"
        const val GRIN = "😀"
        const val PIZZA = "🍕"
    }
}
