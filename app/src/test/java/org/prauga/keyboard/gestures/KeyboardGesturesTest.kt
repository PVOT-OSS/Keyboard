/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.gestures

import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.keyboard.gestures.KeyboardGestures.Gesture

class KeyboardGesturesTest {
    private fun classify(x0: Int, y0: Int, x1: Int, y1: Int) =
        KeyboardGestures.classify(intArrayOf(x0, x1), intArrayOf(y0, y1), 2, 100, 150)

    @Test
    fun leftSwipeDeletesAWord() {
        assertEquals(Gesture.DELETE_WORD, classify(800, 300, 400, 340))
    }

    @Test
    fun downSwipeHidesTheKeyboard() {
        assertEquals(Gesture.HIDE_KEYBOARD, classify(500, 100, 540, 400))
    }

    @Test
    fun shortOrDiagonalStrokesDoNothing() {
        assertEquals(Gesture.NONE, classify(500, 300, 400, 300))
        assertEquals(Gesture.NONE, classify(800, 100, 400, 400))
        assertEquals(Gesture.NONE, classify(400, 300, 800, 300))
    }

    @Test
    fun lastWordIncludesTrailingSpaces() {
        assertEquals(6, KeyboardGestures.lengthOfLastWord("hello world "))
        assertEquals(5, KeyboardGestures.lengthOfLastWord("hello world"))
        assertEquals(5, KeyboardGestures.lengthOfLastWord("hello"))
        assertEquals(0, KeyboardGestures.lengthOfLastWord(""))
    }
}
