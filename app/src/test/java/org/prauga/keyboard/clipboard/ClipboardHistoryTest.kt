/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardHistoryTest {
    @Test
    fun newestFirstAndDuplicatesMoveToTop() {
        val history = ClipboardHistory()
        assertTrue(history.add("a", NOW - 3, false, NOW))
        assertTrue(history.add("b", NOW - 2, false, NOW))
        assertTrue(history.add("a", NOW - 1, false, NOW))
        assertEquals(listOf("a", "b"), history.getItems(NOW).map { it.text })
    }

    @Test
    fun sameClipReadAgainIsIgnored() {
        val history = ClipboardHistory()
        assertTrue(history.add("a", NOW - 1, false, NOW))
        assertFalse(history.add("a", NOW - 1, false, NOW))
    }

    @Test
    fun sensitiveBlankAndOldClipsAreNotStored() {
        val history = ClipboardHistory()
        assertFalse(history.add("secret", NOW, true, NOW))
        assertFalse(history.add("   ", NOW, false, NOW))
        assertFalse(history.add(null, NOW, false, NOW))
        assertFalse(history.add("old", NOW - ClipboardHistory.UNPINNED_LIFETIME_MS - 1, false, NOW))
        assertTrue(history.getItems(NOW).isEmpty())
    }

    @Test
    fun unpinnedItemsExpireButPinnedStay() {
        val history = ClipboardHistory()
        history.add("keep", NOW, false, NOW)
        history.add("drop", NOW, false, NOW)
        history.setPinned("keep", true)
        val later = NOW + ClipboardHistory.UNPINNED_LIFETIME_MS + 1
        assertEquals(listOf("keep"), history.getItems(later).map { it.text })
    }

    @Test
    fun capRemovesOldestUnpinnedFirst() {
        val history = ClipboardHistory()
        history.add("pinned", NOW - 1000, false, NOW)
        history.setPinned("pinned", true)
        repeat(ClipboardHistory.MAX_ITEMS) { history.add("item$it", NOW - 500 + it, false, NOW) }
        val texts = history.getItems(NOW).map { it.text }
        assertEquals(ClipboardHistory.MAX_ITEMS, texts.size)
        assertTrue("pinned" in texts)
        assertFalse("item0" in texts)
    }

    @Test
    fun readdingAPinnedClipKeepsThePin() {
        val history = ClipboardHistory()
        history.add("a", NOW - 2, false, NOW)
        history.setPinned("a", true)
        history.add("a", NOW - 1, false, NOW)
        assertTrue(history.getItems(NOW)[0].pinned)
    }

    @Test
    fun clearUnpinnedKeepsPins() {
        val history = ClipboardHistory()
        history.add("a", NOW, false, NOW)
        history.add("b", NOW, false, NOW)
        history.setPinned("a", true)
        history.clearUnpinned()
        assertEquals(listOf("a"), history.getItems(NOW).map { it.text })
    }

    @Test
    fun restoredPinAppliesToAClipAddedBeforeLoading() {
        val history = ClipboardHistory()
        history.add("a", NOW, false, NOW)
        history.restorePinned(listOf(ClipboardHistory.Item("a", 1, true)))
        val items = history.getItems(NOW)
        assertEquals(1, items.size)
        assertTrue(items[0].pinned)
        assertEquals(NOW, items[0].timestamp)
    }

    @Test
    fun clearedClipIsNotReadBackIn() {
        val history = ClipboardHistory()
        history.add("a", NOW - 1, false, NOW)
        history.clearUnpinned()
        assertFalse(history.add("a", NOW - 1, false, NOW))
        assertTrue(history.add("b", NOW, false, NOW))
    }

    @Test
    fun deletedClipIsNotReadBackIn() {
        val history = ClipboardHistory()
        history.add("a", NOW - 1, false, NOW)
        history.remove("a")
        assertFalse(history.add("a", NOW - 1, false, NOW))
        assertTrue(history.add("a", NOW, false, NOW))
    }

    @Test
    fun restorePinnedSkipsDuplicates() {
        val history = ClipboardHistory()
        history.add("a", NOW, false, NOW)
        history.restorePinned(listOf(ClipboardHistory.Item("a", 1, true)))
        history.restorePinned(listOf(ClipboardHistory.Item("b", 2, true)))
        assertEquals(2, history.getItems(NOW).size)
    }

    @Test
    fun moveToTopKeepsThePin() {
        val history = ClipboardHistory()
        history.add("a", NOW - 2, false, NOW)
        history.add("b", NOW - 1, false, NOW)
        history.setPinned("a", true)
        history.moveToTop("a", NOW)
        val items = history.getItems(NOW)
        assertEquals(listOf("a", "b"), items.map { it.text })
        assertTrue(items[0].pinned)
    }

    @Test
    fun editingReplacesTheClipAndKeepsThePin() {
        val history = ClipboardHistory()
        history.add("helo", NOW - 1, false, NOW)
        history.setPinned("helo", true)
        assertTrue(history.replace("helo", "hello", NOW))
        val items = history.getItems(NOW)
        assertEquals(listOf("hello"), items.map { it.text })
        assertTrue(items[0].pinned)
        assertFalse(history.replace("hello", "  ", NOW))
    }

    @Test
    fun manualClipsGoOnTopWithoutDuplicates() {
        val history = ClipboardHistory()
        history.add("a", NOW - 1, false, NOW)
        assertTrue(history.addManual("b", NOW))
        assertTrue(history.addManual("a", NOW))
        assertEquals(listOf("a", "b"), history.getItems(NOW).map { it.text })
        assertFalse(history.addManual(" ", NOW))
    }

    @Test
    fun shortcutsFindPinnedClipsOnly() {
        val history = ClipboardHistory()
        history.add("221B Baker Street", NOW, false, NOW)
        history.setShortcut("221B Baker Street", " Addr ")
        assertEquals(null, history.findByShortcut("addr"))
        history.setPinned("221B Baker Street", true)
        assertEquals("221B Baker Street", history.findByShortcut("ADDR"))
    }

    @Test
    fun aShortcutBelongsToOneClipAndSurvivesEdits() {
        val history = ClipboardHistory()
        history.addManual("one", NOW, pinned = true)
        history.addManual("two", NOW, pinned = true)
        history.setShortcut("one", "x")
        history.setShortcut("two", "x")
        assertEquals("two", history.findByShortcut("x"))
        history.replace("two", "two!", NOW)
        assertEquals("two!", history.findByShortcut("x"))
        history.setShortcut("two!", "")
        assertEquals(null, history.findByShortcut("x"))
    }

    @Test
    fun imageClipsAreListedByName() {
        val history = ClipboardHistory()
        history.add("clip-1.png", NOW, false, NOW, "image/png")
        history.add("text", NOW, false, NOW)
        assertEquals(setOf("clip-1.png"), history.imageNames())
        assertTrue(history.getItems(NOW).first { it.text == "clip-1.png" }.isImage)
    }

    private companion object {
        const val NOW = 10_000_000L
    }
}
