/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import java.util.concurrent.TimeUnit

class ClipboardHistory {
    data class Item(val text: String, val timestamp: Long, val pinned: Boolean)

    private val items = ArrayList<Item>()
    private var forgottenTimestamp = Long.MIN_VALUE

    fun add(clipText: CharSequence?, timestamp: Long, isSensitive: Boolean, now: Long): Boolean {
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
        val index = indexOf(text)
        if (index >= 0) {
            val existing = items[index]
            if (existing.timestamp >= timestamp) {
                return false
            }
            pinned = existing.pinned
            items.removeAt(index)
        }
        items.add(0, Item(text, timestamp, pinned))
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
                    timestamp = maxOf(existing.timestamp, item.timestamp), pinned = true
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
