/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import org.prauga.keyboard.R
import org.prauga.keyboard.suggestions.InlineSuggestionsStrip
import org.prauga.keyboard.utils.ExecutorUtils

class ClipboardHistoryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    interface Listener {
        fun onClipboardHistoryPaste(text: String)

        fun onClipboardHistoryPasteImage(name: String, mimeType: String)

        fun onClipboardHistoryClose()

        fun onClipboardHistorySearch()

        fun onClipboardHistoryAdd()

        fun onClipboardHistoryEdit(text: String)

        fun onClipboardHistoryShortcut(text: String, shortcut: String?)
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
    private lateinit var shortcut: TextView
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
        shortcut = findViewById(R.id.clipboard_history_shortcut)
        shortcut.setOnClickListener {
            val item = selected
            select(null)
            item?.let { listener.onClipboardHistoryShortcut(it.text, it.shortcut) }
        }
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
        listOf(back, title, clear, empty, pin, edit, toTop, shortcut, delete, cancel).forEach {
            it.setTextColor(tint)
        }
        search.imageTintList = tint
        add.imageTintList = tint
        select(null)
    }

    private fun select(item: ClipboardHistory.Item?) {
        selected = item
        actions.visibility = if (item == null) GONE else VISIBLE
        shortcut.visibility = if (item?.pinned == true && !item.isImage) VISIBLE else GONE
        edit.visibility = if (item?.isImage == true) GONE else VISIBLE
        if (item != null) {
            pin.setText(
                if (item.pinned) R.string.clipboard_history_unpin
                else R.string.clipboard_history_pin
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
            if (item.isImage) {
                items.addView(
                    imageView(item, if (selected?.text == item.text) selectedColor else chipColor)
                )
                continue
            }
            val view = inflater.inflate(R.layout.clipboard_history_item, items, false) as TextView
            view.text = item.text
            view.setTextColor(tint)
            view.backgroundTintList =
                ColorStateList.valueOf(
                    if (selected?.text == item.text) selectedColor else chipColor
                )
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                if (item.pinned) R.drawable.ic_clipboard_pin else 0,
                0,
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

    private fun imageView(item: ClipboardHistory.Item, background: Int): View {
        val view =
            ImageView(context).apply {
                setBackgroundResource(R.drawable.inline_suggestion_chip)
                backgroundTintList = ColorStateList.valueOf(background)
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
                contentDescription = resources.getString(R.string.clipboard_history_image)
                setOnClickListener {
                    item.mimeType?.let { listener.onClipboardHistoryPasteImage(item.text, it) }
                }
                setOnLongClickListener {
                    select(item)
                    true
                }
            }
        val margin = resources.getDimensionPixelSize(R.dimen.clipboard_history_item_margin)
        view.layoutParams =
            GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                )
                .apply {
                    width = 0
                    height = resources.getDimensionPixelSize(R.dimen.clipboard_history_image_height)
                    setMargins(margin, margin, margin, margin)
                }
        val file = ClipImages.fileFor(context, item.text)
        val size = resources.getDimensionPixelSize(R.dimen.clipboard_history_image_height)
        if (file != null) {
            ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
                val bitmap = decodeThumbnail(file.path, size)
                view.post { view.setImageBitmap(bitmap) }
            }
        }
        return view
    }

    private companion object {
        fun decodeThumbnail(path: String, size: Int): Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (
                bounds.outWidth / (sample * 2) >= size && bounds.outHeight / (sample * 2) >= size
            ) {
                sample *= 2
            }
            return BitmapFactory.decodeFile(
                path,
                BitmapFactory.Options().apply { inSampleSize = sample },
            )
        }

        const val SELECTED_ALPHA = 0x66
    }
}
