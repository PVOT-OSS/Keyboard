/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.blockedwords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

public class BlockedWordsTest {
    @Test
    public void normalizeTrimsAndLowercases() {
        assertEquals("hello", BlockedWords.normalize("  Hello "));
    }

    @Test
    public void isBlockedIgnoresCase() {
        final Set<String> blocked = Collections.singleton("hello");
        assertTrue(BlockedWords.isBlocked(blocked, "Hello"));
        assertTrue(BlockedWords.isBlocked(blocked, "HELLO"));
        assertFalse(BlockedWords.isBlocked(blocked, "help"));
    }

    @Test
    public void emptySetBlocksNothing() {
        assertFalse(BlockedWords.isBlocked(Collections.<String>emptySet(), "hello"));
    }
}
