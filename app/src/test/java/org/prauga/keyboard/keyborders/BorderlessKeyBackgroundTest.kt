/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.keyborders

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BorderlessKeyBackgroundTest {
    @Test
    fun idleKeysAreNotDrawn() {
        assertFalse(BorderlessKeyBackground.isHighlighted(intArrayOf()))
        assertFalse(BorderlessKeyBackground.isHighlighted(intArrayOf(android.R.attr.state_checkable)))
    }

    @Test
    fun pressedLockedAndActionKeysAreDrawn() {
        assertTrue(BorderlessKeyBackground.isHighlighted(intArrayOf(android.R.attr.state_pressed)))
        assertTrue(
            BorderlessKeyBackground.isHighlighted(
                intArrayOf(android.R.attr.state_checkable, android.R.attr.state_checked)
            )
        )
        assertTrue(BorderlessKeyBackground.isHighlighted(intArrayOf(android.R.attr.state_active)))
    }
}
