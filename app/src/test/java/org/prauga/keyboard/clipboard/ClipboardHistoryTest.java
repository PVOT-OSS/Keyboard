/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ClipboardHistoryTest {
    private static final long NOW = 10_000_000L;

    @Test
    public void newestFirstAndDuplicatesMoveToTop() {
        final ClipboardHistory history = new ClipboardHistory();
        assertTrue(history.add("a", NOW - 3, false, NOW));
        assertTrue(history.add("b", NOW - 2, false, NOW));
        assertTrue(history.add("a", NOW - 1, false, NOW));
        final List<ClipboardHistory.Item> items = history.getItems(NOW);
        assertEquals(2, items.size());
        assertEquals("a", items.get(0).mText);
        assertEquals("b", items.get(1).mText);
    }

    @Test
    public void sameClipReadAgainIsIgnored() {
        final ClipboardHistory history = new ClipboardHistory();
        assertTrue(history.add("a", NOW - 1, false, NOW));
        assertFalse(history.add("a", NOW - 1, false, NOW));
    }

    @Test
    public void sensitiveBlankAndOldClipsAreNotStored() {
        final ClipboardHistory history = new ClipboardHistory();
        assertFalse(history.add("secret", NOW, true, NOW));
        assertFalse(history.add("   ", NOW, false, NOW));
        assertFalse(history.add(null, NOW, false, NOW));
        assertFalse(history.add("old", NOW - ClipboardHistory.UNPINNED_LIFETIME_MS - 1, false,
                NOW));
        assertTrue(history.getItems(NOW).isEmpty());
    }

    @Test
    public void unpinnedItemsExpireButPinnedStay() {
        final ClipboardHistory history = new ClipboardHistory();
        history.add("keep", NOW, false, NOW);
        history.add("drop", NOW, false, NOW);
        history.setPinned("keep", true);
        final long later = NOW + ClipboardHistory.UNPINNED_LIFETIME_MS + 1;
        final List<ClipboardHistory.Item> items = history.getItems(later);
        assertEquals(1, items.size());
        assertEquals("keep", items.get(0).mText);
    }

    @Test
    public void capRemovesOldestUnpinnedFirst() {
        final ClipboardHistory history = new ClipboardHistory();
        history.add("pinned", NOW - 1000, false, NOW);
        history.setPinned("pinned", true);
        for (int i = 0; i < ClipboardHistory.MAX_ITEMS; i++) {
            history.add("item" + i, NOW - 500 + i, false, NOW);
        }
        final List<ClipboardHistory.Item> items = history.getItems(NOW);
        assertEquals(ClipboardHistory.MAX_ITEMS, items.size());
        assertTrue(items.stream().anyMatch(item -> item.mText.equals("pinned")));
        assertFalse(items.stream().anyMatch(item -> item.mText.equals("item0")));
    }

    @Test
    public void readdingAPinnedClipKeepsThePin() {
        final ClipboardHistory history = new ClipboardHistory();
        history.add("a", NOW - 2, false, NOW);
        history.setPinned("a", true);
        history.add("a", NOW - 1, false, NOW);
        assertTrue(history.getItems(NOW).get(0).mPinned);
    }

    @Test
    public void clearUnpinnedKeepsPins() {
        final ClipboardHistory history = new ClipboardHistory();
        history.add("a", NOW, false, NOW);
        history.add("b", NOW, false, NOW);
        history.setPinned("a", true);
        history.clearUnpinned();
        assertEquals(Collections.singletonList("a"),
                history.getItems(NOW).stream().map(item -> item.mText)
                        .collect(java.util.stream.Collectors.toList()));
    }

    @Test
    public void restoredPinAppliesToAClipAddedBeforeLoading() {
        final ClipboardHistory history = new ClipboardHistory();
        history.add("a", NOW, false, NOW);
        history.restorePinned(Collections.singletonList(new ClipboardHistory.Item("a", 1, true)));
        final List<ClipboardHistory.Item> items = history.getItems(NOW);
        assertEquals(1, items.size());
        assertTrue(items.get(0).mPinned);
        assertEquals(NOW, items.get(0).mTimestamp);
    }

    @Test
    public void restorePinnedSkipsDuplicates() {
        final ClipboardHistory history = new ClipboardHistory();
        history.add("a", NOW, false, NOW);
        history.restorePinned(Collections.singletonList(new ClipboardHistory.Item("a", 1, true)));
        history.restorePinned(Collections.singletonList(new ClipboardHistory.Item("b", 2, true)));
        assertEquals(2, history.getItems(NOW).size());
    }
}
