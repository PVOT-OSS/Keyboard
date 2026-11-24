/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.onehanded

import android.content.SharedPreferences
import kotlin.math.roundToInt

object OneHandedMode {
    const val PREF_ONE_HANDED_MODE = "pref_one_handed_mode"
    const val PREF_ONE_HANDED_SIDE = "pref_one_handed_side"

    const val OFF = 0
    const val LEFT = 1
    const val RIGHT = 2

    private const val VALUE_OFF = "off"
    private const val VALUE_LEFT = "left"
    private const val VALUE_RIGHT = "right"

    @JvmStatic
    fun read(prefs: SharedPreferences): Int =
        fromValue(prefs.getString(PREF_ONE_HANDED_MODE, VALUE_OFF))

    @JvmStatic
    fun toggle(prefs: SharedPreferences) {
        val mode = if (read(prefs) == OFF) {
            sideOrDefault(fromValue(prefs.getString(PREF_ONE_HANDED_SIDE, VALUE_RIGHT)))
        } else {
            OFF
        }
        write(prefs, mode)
    }

    @JvmStatic
    fun switchSide(prefs: SharedPreferences) {
        write(prefs, otherSide(read(prefs)))
    }

    @JvmStatic
    fun turnOff(prefs: SharedPreferences) {
        write(prefs, OFF)
    }

    @JvmStatic
    fun keyboardWidth(screenWidth: Int, widthFraction: Float): Int =
        (screenWidth * widthFraction).roundToInt()

    private fun write(prefs: SharedPreferences, mode: Int) {
        val editor = prefs.edit().putString(PREF_ONE_HANDED_MODE, toValue(mode))
        if (mode != OFF) {
            editor.putString(PREF_ONE_HANDED_SIDE, toValue(mode))
        }
        editor.apply()
    }

    internal fun fromValue(value: String?): Int = when (value) {
        VALUE_LEFT -> LEFT
        VALUE_RIGHT -> RIGHT
        else -> OFF
    }

    internal fun toValue(mode: Int): String = when (mode) {
        LEFT -> VALUE_LEFT
        RIGHT -> VALUE_RIGHT
        else -> VALUE_OFF
    }

    internal fun otherSide(mode: Int): Int = if (mode == LEFT) RIGHT else LEFT

    internal fun sideOrDefault(mode: Int): Int = if (mode == OFF) RIGHT else mode
}
