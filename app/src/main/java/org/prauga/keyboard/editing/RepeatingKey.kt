/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.editing

import android.annotation.SuppressLint
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import org.prauga.keyboard.R

object RepeatingKey {
    @SuppressLint("ClickableViewAccessibility")
    fun attach(key: View, handler: Handler, action: () -> Unit) {
        val startDelay = key.resources.getInteger(R.integer.config_key_repeat_start_timeout).toLong()
        val interval = key.resources.getInteger(R.integer.config_key_repeat_interval).toLong()
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
}
