/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.textfield

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import org.prauga.keyboard.R

class StripTextField(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    fun interface CloseListener {
        fun onTextFieldClose()
    }

    private val text = StringBuilder()
    private var closeListener: CloseListener? = null
    private var client: StripTextFieldClient? = null
    private lateinit var tint: ColorStateList
    private lateinit var close: ImageButton
    private lateinit var textView: TextView
    private lateinit var scroller: HorizontalScrollView
    private lateinit var results: LinearLayout

    override fun onFinishInflate() {
        super.onFinishInflate()
        close = findViewById(R.id.strip_text_field_close)
        textView = findViewById(R.id.strip_text_field_text)
        scroller = findViewById(R.id.strip_text_field_scroller)
        results = findViewById(R.id.strip_text_field_results)
        close.setOnClickListener { closeListener?.onTextFieldClose() }
    }

    fun setCloseListener(listener: CloseListener) {
        closeListener = listener
    }

    fun start(client: StripTextFieldClient, tint: ColorStateList) {
        this.client = client
        this.tint = tint
        close.imageTintList = tint
        textView.setTextColor(tint)
        textView.setHintTextColor(ColorUtils.setAlphaComponent(tint.defaultColor, HINT_ALPHA))
        text.setLength(0)
        text.append(client.initialText)
        refresh()
    }

    fun type(input: String) {
        text.append(input)
        refresh()
    }

    fun deleteLast() {
        if (text.isEmpty()) {
            return
        }
        text.delete(text.offsetByCodePoints(text.length, -1), text.length)
        refresh()
    }

    private fun refresh() {
        val client = client ?: return
        textView.text = text
        textView.hint = if (text.isEmpty()) resources.getString(client.hint) else null
        results.removeAllViews()
        scroller.scrollTo(0, 0)
        if (text.isBlank()) {
            return
        }
        val found = client.results(text.toString())
        if (found.isEmpty()) {
            results.addView(
                TextView(context).apply {
                    setText(client.noResults)
                    setTextColor(tint)
                    gravity = Gravity.CENTER_VERTICAL
                },
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT),
            )
            return
        }
        val padding = resources.getDimensionPixelSize(R.dimen.emoji_search_result_padding)
        val textSize = resources.getDimension(R.dimen.emoji_search_result_text_size)
        for (result in found) {
            results.addView(
                TextView(context, null, R.attr.suggestionWordStyle).apply {
                    setText(result.label)
                    contentDescription = result.description
                    gravity = Gravity.CENTER
                    isSingleLine = true
                    setPadding(padding, 0, padding, 0)
                    if (client.largeResults) {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                    } else {
                        setTextColor(tint)
                    }
                    setOnClickListener { result.onPick() }
                },
                LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
        }
    }

    private companion object {
        const val HINT_ALPHA = 0x99
    }
}
