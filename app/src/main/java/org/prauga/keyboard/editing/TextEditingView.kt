/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.editing

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import org.prauga.keyboard.R
import org.prauga.keyboard.suggestions.InlineSuggestionsStrip

class TextEditingView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    interface Listener {
        fun onEditingKey(keyCode: Int, extendSelection: Boolean)

        fun onEditingAction(actionId: Int)

        fun onTextEditingClose()
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var listener: Listener
    private lateinit var tint: ColorStateList
    private lateinit var back: TextView
    private lateinit var select: TextView
    private lateinit var keys: ViewGroup
    private var selecting = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        back = findViewById(R.id.text_editing_back)
        select = findViewById(R.id.text_editing_select)
        keys = findViewById(R.id.text_editing_keys)
        back.setOnClickListener { listener.onTextEditingClose() }
        select.setOnClickListener { setSelecting(!selecting) }
        setUpMoveKey(R.id.text_editing_left, KeyEvent.KEYCODE_DPAD_LEFT)
        setUpMoveKey(R.id.text_editing_right, KeyEvent.KEYCODE_DPAD_RIGHT)
        setUpMoveKey(R.id.text_editing_up, KeyEvent.KEYCODE_DPAD_UP)
        setUpMoveKey(R.id.text_editing_down, KeyEvent.KEYCODE_DPAD_DOWN)
        setUpMoveKey(R.id.text_editing_line_start, KeyEvent.KEYCODE_MOVE_HOME)
        setUpMoveKey(R.id.text_editing_line_end, KeyEvent.KEYCODE_MOVE_END)
        setUpRepeatingKey(findViewById(R.id.text_editing_delete)) {
            listener.onEditingKey(KeyEvent.KEYCODE_DEL, false)
        }
        setUpAction(R.id.text_editing_select_all, android.R.id.selectAll, endsSelection = false)
        setUpAction(R.id.text_editing_copy, android.R.id.copy, endsSelection = true)
        setUpAction(R.id.text_editing_cut, android.R.id.cut, endsSelection = true)
        setUpAction(R.id.text_editing_paste, android.R.id.paste, endsSelection = true)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun show(backLabel: CharSequence?, tint: ColorStateList) {
        this.tint = tint
        back.text = backLabel
        back.setTextColor(tint)
        findViewById<TextView>(R.id.text_editing_title).setTextColor(tint)
        for (key in keys.children) {
            when (key) {
                is TextView -> key.setTextColor(tint)
                is ImageButton -> key.imageTintList = tint
            }
        }
        setSelecting(false)
    }

    private fun setSelecting(selecting: Boolean) {
        this.selecting = selecting
        val chipColor = InlineSuggestionsStrip.getChipBackgroundColor(tint.defaultColor)
        val selectedColor = ColorUtils.setAlphaComponent(tint.defaultColor, SELECTED_ALPHA)
        for (key in keys.children) {
            key.backgroundTintList = ColorStateList.valueOf(
                if (key === select && selecting) selectedColor else chipColor
            )
        }
        select.isActivated = selecting
    }

    private fun setUpMoveKey(viewId: Int, keyCode: Int) {
        setUpRepeatingKey(findViewById(viewId)) { listener.onEditingKey(keyCode, selecting) }
    }

    private fun setUpAction(viewId: Int, actionId: Int, endsSelection: Boolean) {
        findViewById<View>(viewId).setOnClickListener {
            listener.onEditingAction(actionId)
            if (endsSelection) {
                setSelecting(false)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpRepeatingKey(key: View, action: () -> Unit) {
        val startDelay = resources.getInteger(R.integer.config_key_repeat_start_timeout).toLong()
        val interval = resources.getInteger(R.integer.config_key_repeat_interval).toLong()
        var repeated = false
        val repeat = object : Runnable {
            override fun run() {
                repeated = true
                action()
                handler.postDelayed(this, interval)
            }
        }
        key.setOnClickListener { action() }
        key.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    repeated = false
                    handler.postDelayed(repeat, startDelay)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(repeat)
                    if (repeated) {
                        view.isPressed = false
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    private companion object {
        const val SELECTED_ALPHA = 0x66
    }
}
