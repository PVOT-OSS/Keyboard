/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.resize

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.prauga.keyboard.R
import org.prauga.keyboard.suggestions.InlineSuggestionsStrip
import kotlin.math.abs
import kotlin.math.roundToInt

class KeyboardResizeView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    interface Listener {
        fun onKeyboardResize(scale: Float)

        fun onKeyboardResizeReset()

        fun onKeyboardResizeDone()
    }

    private val keyboardRect = Rect()
    private var keyboardView: View? = null
    private var showing = false
    private var scale = 1f
    private var dragStartY = 0f
    private var dragStartHeight = 0
    private var dragStartScale = 1f

    val isShowing: Boolean
        get() = showing

    fun bind(keyboardView: View, listener: Listener) {
        this.keyboardView = keyboardView
        findViewById<View>(R.id.keyboard_resize_reset).setOnClickListener {
            listener.onKeyboardResizeReset()
        }
        findViewById<View>(R.id.keyboard_resize_done).setOnClickListener {
            listener.onKeyboardResizeDone()
        }
        setUpHandle(findViewById(R.id.keyboard_resize_handle), listener)
        keyboardView.viewTreeObserver.addOnGlobalLayoutListener { syncWithKeyboard() }
    }

    fun show(scale: Float, tint: ColorStateList) {
        this.scale = scale
        showing = true
        background = keyboardView?.background?.constantState?.newDrawable()
        val chipColor = ColorStateList.valueOf(
            InlineSuggestionsStrip.getChipBackgroundColor(tint.defaultColor)
        )
        findViewById<TextView>(R.id.keyboard_resize_hint).setTextColor(tint)
        for (id in intArrayOf(R.id.keyboard_resize_reset, R.id.keyboard_resize_done)) {
            findViewById<TextView>(id).apply {
                setTextColor(tint)
                backgroundTintList = chipColor
            }
        }
        findViewById<View>(R.id.keyboard_resize_pill).backgroundTintList = tint
        syncWithKeyboard()
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun hide() {
        showing = false
        syncWithKeyboard()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpHandle(handle: View, listener: Listener) {
        handle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dragStartY = event.rawY
                    dragStartHeight = keyboardView?.height ?: 0
                    dragStartScale = scale
                }
                MotionEvent.ACTION_MOVE -> {
                    if (dragStartHeight > 0) {
                        val height = dragStartHeight - (event.rawY - dragStartY)
                        val newScale = roundToStep(dragStartScale * height / dragStartHeight)
                        if (abs(newScale - scale) >= STEP / 2) {
                            scale = newScale
                            listener.onKeyboardResize(newScale)
                        }
                    }
                }
            }
            true
        }
    }

    private fun syncWithKeyboard() {
        val keyboard = keyboardView
        if (keyboard == null || !showing || !keyboard.isShown) {
            if (visibility != GONE) {
                visibility = GONE
            }
            return
        }
        keyboardRect.set(0, 0, keyboard.width, keyboard.height)
        (parent as ViewGroup).offsetDescendantRectToMyCoords(keyboard, keyboardRect)
        val params = layoutParams as FrameLayout.LayoutParams
        if (params.width != keyboardRect.width() || params.height != keyboardRect.height() ||
            params.leftMargin != keyboardRect.left || params.topMargin != keyboardRect.top
        ) {
            params.width = keyboardRect.width()
            params.height = keyboardRect.height()
            params.leftMargin = keyboardRect.left
            params.topMargin = keyboardRect.top
            params.gravity = Gravity.TOP or Gravity.LEFT
            layoutParams = params
        }
        if (visibility != VISIBLE) {
            visibility = VISIBLE
        }
    }

    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 1.2f
        private const val STEP = 0.01f

        internal fun roundToStep(scale: Float): Float =
            ((scale.coerceIn(MIN_SCALE, MAX_SCALE) / STEP).roundToInt() * STEP)
    }
}
