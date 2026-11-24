/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.junit.Test;

public class EmojiKeywordsTest {
    @Test
    public void parsesKeywordsAndSkipsCommentsAndBadLines() throws IOException {
        final Map<String, String> keywords = EmojiKeywords.parse(new StringReader(
                "# header\npizza\t🍕\n\nbroken\nheart\t❤️\nempty\t\n"));
        assertEquals(2, keywords.size());
        assertEquals("🍕", keywords.get("pizza"));
        assertEquals("❤️", keywords.get("heart"));
        assertFalse(keywords.containsKey("broken"));
        assertFalse(keywords.containsKey("empty"));
    }
}
