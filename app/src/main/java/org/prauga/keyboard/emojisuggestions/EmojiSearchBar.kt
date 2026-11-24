/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions

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

class EmojiSearchBar(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    interface Listener {
        fun onEmojiSearchResult(emoji: String)

        fun onEmojiSearchClose()
    }

    private val query = StringBuilder()
    private var listener: Listener? = null
    private lateinit var tint: ColorStateList
    private lateinit var close: ImageButton
    private lateinit var queryView: TextView
    private lateinit var scroller: HorizontalScrollView
    private lateinit var results: LinearLayout

    override fun onFinishInflate() {
        super.onFinishInflate()
        close = findViewById(R.id.emoji_search_close)
        queryView = findViewById(R.id.emoji_search_query)
        scroller = findViewById(R.id.emoji_search_scroller)
        results = findViewById(R.id.emoji_search_results)
        close.setOnClickListener { listener?.onEmojiSearchClose() }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun start(tint: ColorStateList) {
        this.tint = tint
        close.imageTintList = tint
        queryView.setTextColor(tint)
        queryView.setHintTextColor(ColorUtils.setAlphaComponent(tint.defaultColor, HINT_ALPHA))
        query.setLength(0)
        refresh()
    }

    fun type(text: String) {
        query.append(text)
        refresh()
    }

    fun deleteLast() {
        if (query.isEmpty()) {
            return
        }
        query.delete(query.offsetByCodePoints(query.length, -1), query.length)
        refresh()
    }

    private fun refresh() {
        queryView.text = query
        queryView.hint = if (query.isEmpty()) resources.getString(R.string.emoji_search_hint) else null
        results.removeAllViews()
        scroller.scrollTo(0, 0)
        if (query.isBlank()) {
            return
        }
        val emojis = EmojiKeywords.search(query.toString())
        if (emojis.isEmpty()) {
            results.addView(TextView(context).apply {
                setText(R.string.emoji_search_no_results)
                setTextColor(tint)
                gravity = Gravity.CENTER_VERTICAL
            }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
            return
        }
        val padding = resources.getDimensionPixelSize(R.dimen.emoji_search_result_padding)
        val textSize = resources.getDimension(R.dimen.emoji_search_result_text_size)
        for (emoji in emojis) {
            results.addView(TextView(context, null, R.attr.suggestionWordStyle).apply {
                text = emoji
                contentDescription = emoji
                gravity = Gravity.CENTER
                setPadding(padding, 0, padding, 0)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                setOnClickListener { listener?.onEmojiSearchResult(emoji) }
            }, LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    private companion object {
        const val HINT_ALPHA = 0x99
    }
}
