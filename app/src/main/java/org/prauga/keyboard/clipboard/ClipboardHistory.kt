/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import java.util.Locale
import java.util.concurrent.TimeUnit

class ClipboardHistory {
    data class Item(
        val text: String,
        val timestamp: Long,
        val pinned: Boolean,
        val shortcut: String? = null,
        val mimeType: String? = null,
    ) {
        val isImage: Boolean
            get() = mimeType != null
    }

    private val items = ArrayList<Item>()
    private var forgottenTimestamp = Long.MIN_VALUE

    fun add(
        clipText: CharSequence?,
        timestamp: Long,
        isSensitive: Boolean,
        now: Long,
        mimeType: String? = null,
    ): Boolean {
        if (clipText == null || isSensitive || now - timestamp > UNPINNED_LIFETIME_MS ||
            timestamp <= forgottenTimestamp
        ) {
            return false
        }
        val text = clipText.toString()
        if (text.isBlank()) {
            return false
        }
        var pinned = false
        var shortcut: String? = null
        val index = indexOf(text)
        if (index >= 0) {
            val existing = items[index]
            if (existing.timestamp >= timestamp) {
                return false
            }
            pinned = existing.pinned
            shortcut = existing.shortcut
            items.removeAt(index)
        }
        items.add(0, Item(text, timestamp, pinned, shortcut, mimeType))
        trimToMaxItems()
        return true
    }

    fun restorePinned(pinnedItems: List<Item>) {
        for (item in pinnedItems) {
            val index = indexOf(item.text)
            if (index < 0) {
                items.add(item.copy(pinned = true))
            } else {
                val existing = items[index]
                items[index] = existing.copy(
                    timestamp = maxOf(existing.timestamp, item.timestamp),
                    pinned = true,
                    shortcut = item.shortcut ?: existing.shortcut,
                    mimeType = item.mimeType ?: existing.mimeType,
                )
            }
        }
        items.sortByDescending { it.timestamp }
        trimToMaxItems()
    }

    fun getItems(now: Long): List<Item> {
        items.removeAll { !it.pinned && now - it.timestamp > UNPINNED_LIFETIME_MS }
        return items.toList()
    }

    fun getPinnedItems(): List<Item> = items.filter { it.pinned }

    fun imageNames(): Set<String> = items.filter { it.isImage }.mapTo(HashSet()) { it.text }

    fun setShortcut(text: String, shortcut: String?) {
        val normalized = shortcut?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() }
        for (i in items.indices) {
            val item = items[i]
            if (item.text == text) {
                items[i] = item.copy(shortcut = normalized)
            } else if (normalized != null && item.shortcut == normalized) {
                items[i] = item.copy(shortcut = null)
            }
        }
    }

    fun findByShortcut(word: String): String? {
        val key = word.trim().lowercase(Locale.ROOT)
        return items.firstOrNull { it.pinned && it.shortcut == key }?.text
    }

    fun moveToTop(text: String, now: Long) {
        val index = indexOf(text)
        if (index >= 0) {
            items.add(0, items.removeAt(index).copy(timestamp = now))
        }
    }

    fun replace(oldText: String, newText: String, now: Long): Boolean {
        if (newText.isBlank()) {
            return false
        }
        val index = indexOf(oldText)
        val old = if (index >= 0) items.removeAt(index) else null
        addManual(newText, now, old?.pinned == true)
        if (old?.shortcut != null) {
            setShortcut(newText, old.shortcut)
        }
        return true
    }

    fun addManual(text: String, now: Long, pinned: Boolean = false): Boolean {
        if (text.isBlank()) {
            return false
        }
        val index = indexOf(text)
        val existing = if (index >= 0) items.removeAt(index) else null
        items.add(
            0,
            Item(text, now, pinned || existing?.pinned == true, existing?.shortcut, existing?.mimeType)
        )
        trimToMaxItems()
        return true
    }

    fun setPinned(text: String, pinned: Boolean) {
        val index = indexOf(text)
        if (index >= 0) {
            items[index] = items[index].copy(pinned = pinned)
        }
    }

    fun remove(text: String) {
        val index = indexOf(text)
        if (index >= 0) {
            forget(items.removeAt(index))
        }
    }

    fun clearUnpinned() {
        items.filter { !it.pinned }.forEach(::forget)
        items.removeAll { !it.pinned }
    }

    fun clearAll() {
        items.forEach(::forget)
        items.clear()
    }

    private fun forget(item: Item) {
        forgottenTimestamp = maxOf(forgottenTimestamp, item.timestamp)
    }

    private fun indexOf(text: String) = items.indexOfFirst { it.text == text }

    private fun trimToMaxItems() {
        var i = items.size - 1
        while (i >= 0 && items.size > MAX_ITEMS) {
            if (!items[i].pinned) {
                items.removeAt(i)
            }
            i--
        }
        while (items.size > MAX_ITEMS) {
            items.removeAt(items.size - 1)
        }
    }

    companion object {
        const val MAX_ITEMS = 25
        val UNPINNED_LIFETIME_MS = TimeUnit.HOURS.toMillis(1)
    }
}
