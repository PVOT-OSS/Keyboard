/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import org.prauga.keyboard.R
import org.prauga.keyboard.suggestions.InlineSuggestionsStrip

class ClipboardHistoryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    interface Listener {
        fun onClipboardHistoryPaste(text: String)

        fun onClipboardHistoryClose()

        fun onClipboardHistorySearch()

        fun onClipboardHistoryAdd()

        fun onClipboardHistoryEdit(text: String)
    }

    private lateinit var manager: ClipboardHistoryManager
    private lateinit var listener: Listener
    private lateinit var tint: ColorStateList
    private lateinit var items: GridLayout
    private lateinit var empty: TextView
    private lateinit var back: TextView
    private lateinit var title: TextView
    private lateinit var clear: TextView
    private lateinit var actions: View
    private lateinit var pin: TextView
    private lateinit var delete: TextView
    private lateinit var cancel: TextView
    private lateinit var edit: TextView
    private lateinit var toTop: TextView
    private lateinit var search: ImageButton
    private lateinit var add: ImageButton
    private var selected: ClipboardHistory.Item? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        items = findViewById(R.id.clipboard_history_items)
        empty = findViewById(R.id.clipboard_history_empty)
        back = findViewById(R.id.clipboard_history_back)
        title = findViewById(R.id.clipboard_history_title)
        clear = findViewById(R.id.clipboard_history_clear)
        actions = findViewById(R.id.clipboard_history_actions)
        pin = findViewById(R.id.clipboard_history_pin)
        delete = findViewById(R.id.clipboard_history_delete)
        cancel = findViewById(R.id.clipboard_history_cancel)
        edit = findViewById(R.id.clipboard_history_edit)
        toTop = findViewById(R.id.clipboard_history_to_top)
        search = findViewById(R.id.clipboard_history_search)
        add = findViewById(R.id.clipboard_history_add)
        search.setOnClickListener { listener.onClipboardHistorySearch() }
        add.setOnClickListener { listener.onClipboardHistoryAdd() }
        edit.setOnClickListener {
            val text = selected?.text
            select(null)
            text?.let(listener::onClipboardHistoryEdit)
        }
        toTop.setOnClickListener {
            selected?.let { manager.moveToTop(it.text) }
            select(null)
        }
        back.setOnClickListener { listener.onClipboardHistoryClose() }
        clear.setOnClickListener {
            manager.clearUnpinned()
            refresh()
        }
        pin.setOnClickListener {
            selected?.let { manager.setPinned(it.text, !it.pinned) }
            select(null)
        }
        delete.setOnClickListener {
            selected?.let { manager.remove(it.text) }
            select(null)
        }
        cancel.setOnClickListener { select(null) }
    }

    fun setUp(manager: ClipboardHistoryManager, listener: Listener) {
        this.manager = manager
        this.listener = listener
    }

    fun show(backLabel: CharSequence?, tint: ColorStateList) {
        this.tint = tint
        back.text = backLabel
        listOf(back, title, clear, empty, pin, edit, toTop, delete, cancel).forEach { it.setTextColor(tint) }
        search.imageTintList = tint
        add.imageTintList = tint
        select(null)
    }

    private fun select(item: ClipboardHistory.Item?) {
        selected = item
        actions.visibility = if (item == null) GONE else VISIBLE
        if (item != null) {
            pin.setText(
                if (item.pinned) R.string.clipboard_history_unpin else R.string.clipboard_history_pin
            )
        }
        refresh()
    }

    private fun refresh() {
        items.removeAllViews()
        val history = manager.getItems()
        empty.visibility = if (history.isEmpty()) VISIBLE else GONE
        val inflater = LayoutInflater.from(context)
        val chipColor = InlineSuggestionsStrip.getChipBackgroundColor(tint.defaultColor)
        val selectedColor = ColorUtils.setAlphaComponent(tint.defaultColor, SELECTED_ALPHA)
        for (item in history) {
            val view = inflater.inflate(R.layout.clipboard_history_item, items, false) as TextView
            view.text = item.text
            view.setTextColor(tint)
            view.backgroundTintList = ColorStateList.valueOf(
                if (selected?.text == item.text) selectedColor else chipColor
            )
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, if (item.pinned) R.drawable.ic_clipboard_pin else 0, 0
            )
            view.compoundDrawableTintList = tint
            view.setOnClickListener { listener.onClipboardHistoryPaste(item.text) }
            view.setOnLongClickListener {
                select(item)
                true
            }
            items.addView(view)
        }
        clear.visibility = if (history.any { !it.pinned }) VISIBLE else INVISIBLE
    }

    private companion object {
        const val SELECTED_ALPHA = 0x66
    }
}
