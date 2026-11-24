/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.keyborders

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper

class BorderlessKeyBackground(original: Drawable) : DrawableWrapper(original) {
    override fun draw(canvas: Canvas) {
        if (isHighlighted(state)) {
            super.draw(canvas)
        }
    }

    override fun isStateful(): Boolean = true

    companion object {
        const val PREF_KEY_BORDERS = "pref_key_borders"

        private val HIGHLIGHT_STATES = intArrayOf(
            android.R.attr.state_pressed,
            android.R.attr.state_checked,
            android.R.attr.state_active,
        )

        internal fun isHighlighted(state: IntArray): Boolean = state.any { it in HIGHLIGHT_STATES }
    }
}
