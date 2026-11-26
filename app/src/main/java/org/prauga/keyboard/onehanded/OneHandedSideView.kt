/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.onehanded

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import org.prauga.keyboard.R

class OneHandedSideView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    interface Listener {
        fun onOneHandedSwitchSide()

        fun onOneHandedExit()
    }

    private val keyboardRect = Rect()
    private var keyboardView: View? = null
    private lateinit var switchSide: ImageButton
    private lateinit var exit: ImageButton
    private var mode = OneHandedMode.OFF
    private var sideWidth = 0

    override fun onFinishInflate() {
        super.onFinishInflate()
        switchSide = findViewById(R.id.one_handed_switch_side)
        exit = findViewById(R.id.one_handed_exit)
    }

    fun bind(keyboardView: View, listener: Listener) {
        this.keyboardView = keyboardView
        switchSide.setOnClickListener { listener.onOneHandedSwitchSide() }
        exit.setOnClickListener { listener.onOneHandedExit() }
        keyboardView.viewTreeObserver.addOnGlobalLayoutListener { syncWithKeyboard() }
    }

    fun setMode(mode: Int, sideWidth: Int, tint: ColorStateList) {
        this.mode = mode
        this.sideWidth = sideWidth
        switchSide.setImageResource(
            if (mode == OneHandedMode.LEFT) {
                R.drawable.ic_editing_chevron_right
            } else {
                R.drawable.ic_editing_chevron_left
            }
        )
        switchSide.imageTintList = tint
        exit.imageTintList = tint
        syncWithKeyboard()
    }

    private fun syncWithKeyboard() {
        val keyboard = keyboardView
        val hidden = mode == OneHandedMode.OFF || sideWidth <= 0 || keyboard?.isShown != true
        if (keyboard == null || hidden) {
            if (visibility != GONE) {
                visibility = GONE
            }
            return
        }
        keyboardRect.set(0, 0, keyboard.width, keyboard.height)
        (parent as ViewGroup).offsetDescendantRectToMyCoords(keyboard, keyboardRect)
        val left =
            if (mode == OneHandedMode.LEFT) {
                keyboardRect.right - sideWidth
            } else {
                keyboardRect.left
            }
        val params = layoutParams as FrameLayout.LayoutParams
        val placed =
            params.width == sideWidth &&
                params.height == keyboardRect.height() &&
                params.leftMargin == left &&
                params.topMargin == keyboardRect.top
        if (!placed) {
            params.width = sideWidth
            params.height = keyboardRect.height()
            params.leftMargin = left
            params.topMargin = keyboardRect.top
            params.gravity = Gravity.TOP or Gravity.LEFT
            layoutParams = params
        }
        if (visibility != VISIBLE) {
            visibility = VISIBLE
        }
    }
}
