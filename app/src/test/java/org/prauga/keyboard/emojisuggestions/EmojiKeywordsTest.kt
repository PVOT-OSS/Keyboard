/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.StringReader

class EmojiKeywordsTest {
    @Test
    fun parsesKeywordsAndSkipsCommentsAndBadLines() {
        val keywords = EmojiKeywords.parse(
            StringReader("# header\npizza\t🍕\n\nbroken\nheart\t❤️\nempty\t\n")
        )
        assertEquals(mapOf("pizza" to "🍕", "heart" to "❤️"), keywords)
    }
}
