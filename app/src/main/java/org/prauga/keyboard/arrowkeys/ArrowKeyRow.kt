/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.arrowkeys

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.LinearLayout
import org.prauga.keyboard.R
import org.prauga.keyboard.editing.RepeatingKey

class ArrowKeyRow(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    fun interface Listener {
        fun onArrowKey(keyCode: Int)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val keys = listOf(
        R.id.arrow_key_left to KeyEvent.KEYCODE_DPAD_LEFT,
        R.id.arrow_key_up to KeyEvent.KEYCODE_DPAD_UP,
        R.id.arrow_key_down to KeyEvent.KEYCODE_DPAD_DOWN,
        R.id.arrow_key_right to KeyEvent.KEYCODE_DPAD_RIGHT,
    )

    fun bind(listener: Listener) {
        for ((id, keyCode) in keys) {
            RepeatingKey.attach(findViewById(id), handler) { listener.onArrowKey(keyCode) }
        }
    }

    fun setTint(tint: ColorStateList) {
        for ((id, _) in keys) {
            findViewById<ImageButton>(id).imageTintList = tint
        }
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }
}
