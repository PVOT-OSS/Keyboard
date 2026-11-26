/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.gestures

import kotlin.math.abs

object KeyboardGestures {
    const val PREF_KEYBOARD_GESTURES = "pref_keyboard_gestures"

    enum class Gesture {
        NONE,
        DELETE_WORD,
        HIDE_KEYBOARD,
    }

    private const val MIN_TRAVEL_IN_KEYS = 1.5f
    private const val MAX_DRIFT_IN_KEYS = 1.0f

    @JvmStatic
    fun classify(xs: IntArray, ys: IntArray, size: Int, keyWidth: Int, keyHeight: Int): Gesture {
        if (size < 2 || keyWidth <= 0 || keyHeight <= 0) {
            return Gesture.NONE
        }
        val dx = (xs[size - 1] - xs[0]).toFloat()
        val dy = (ys[size - 1] - ys[0]).toFloat()
        return when {
            dx < -MIN_TRAVEL_IN_KEYS * keyWidth && abs(dy) < MAX_DRIFT_IN_KEYS * keyHeight ->
                Gesture.DELETE_WORD
            dy > MIN_TRAVEL_IN_KEYS * keyHeight && abs(dx) < MAX_DRIFT_IN_KEYS * keyWidth ->
                Gesture.HIDE_KEYBOARD
            else -> Gesture.NONE
        }
    }

    @JvmStatic
    fun lengthOfLastWord(textBeforeCursor: CharSequence): Int {
        var end = textBeforeCursor.length
        while (end > 0 && Character.isWhitespace(textBeforeCursor[end - 1])) {
            end--
        }
        var start = end
        while (start > 0 && !Character.isWhitespace(textBeforeCursor[start - 1])) {
            start--
        }
        return textBeforeCursor.length - start
    }
}
