/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ClipboardHistory {
    public static final int MAX_ITEMS = 25;
    static final long UNPINNED_LIFETIME_MS = TimeUnit.HOURS.toMillis(1);

    public static final class Item {
        @Nonnull
        public final String mText;
        public final long mTimestamp;
        public final boolean mPinned;

        public Item(@Nonnull final String text, final long timestamp, final boolean pinned) {
            mText = text;
            mTimestamp = timestamp;
            mPinned = pinned;
        }
    }

    private final ArrayList<Item> mItems = new ArrayList<>();
    private long mForgottenTimestamp = Long.MIN_VALUE;

    public boolean add(@Nullable final CharSequence clipText, final long timestamp,
            final boolean isSensitive, final long now) {
        if (clipText == null || isSensitive || now - timestamp > UNPINNED_LIFETIME_MS
                || timestamp <= mForgottenTimestamp) {
            return false;
        }
        final String text = clipText.toString();
        if (text.trim().isEmpty()) {
            return false;
        }
        boolean pinned = false;
        final int index = indexOf(text);
        if (index >= 0) {
            final Item existing = mItems.get(index);
            if (existing.mTimestamp >= timestamp) {
                return false;
            }
            pinned = existing.mPinned;
            mItems.remove(index);
        }
        mItems.add(0, new Item(text, timestamp, pinned));
        trimToMaxItems();
        return true;
    }

    public void restorePinned(@Nonnull final List<Item> pinnedItems) {
        for (final Item item : pinnedItems) {
            final int index = indexOf(item.mText);
            if (index < 0) {
                mItems.add(new Item(item.mText, item.mTimestamp, true));
            } else {
                final Item existing = mItems.get(index);
                mItems.set(index, new Item(existing.mText,
                        Math.max(existing.mTimestamp, item.mTimestamp), true));
            }
        }
        Collections.sort(mItems, (a, b) -> Long.compare(b.mTimestamp, a.mTimestamp));
        trimToMaxItems();
    }

    @Nonnull
    public List<Item> getItems(final long now) {
        final Iterator<Item> iterator = mItems.iterator();
        while (iterator.hasNext()) {
            final Item item = iterator.next();
            if (!item.mPinned && now - item.mTimestamp > UNPINNED_LIFETIME_MS) {
                iterator.remove();
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(mItems));
    }

    @Nonnull
    public List<Item> getPinnedItems() {
        final List<Item> pinned = new ArrayList<>();
        for (final Item item : mItems) {
            if (item.mPinned) {
                pinned.add(item);
            }
        }
        return pinned;
    }

    public void setPinned(@Nonnull final String text, final boolean pinned) {
        final int index = indexOf(text);
        if (index >= 0) {
            final Item item = mItems.get(index);
            mItems.set(index, new Item(item.mText, item.mTimestamp, pinned));
        }
    }

    public void remove(@Nonnull final String text) {
        final int index = indexOf(text);
        if (index >= 0) {
            forget(mItems.remove(index));
        }
    }

    public void clearUnpinned() {
        final Iterator<Item> iterator = mItems.iterator();
        while (iterator.hasNext()) {
            final Item item = iterator.next();
            if (!item.mPinned) {
                forget(item);
                iterator.remove();
            }
        }
    }

    public void clearAll() {
        for (final Item item : mItems) {
            forget(item);
        }
        mItems.clear();
    }

    private void forget(@Nonnull final Item item) {
        mForgottenTimestamp = Math.max(mForgottenTimestamp, item.mTimestamp);
    }

    private int indexOf(@Nonnull final String text) {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).mText.equals(text)) {
                return i;
            }
        }
        return -1;
    }

    private void trimToMaxItems() {
        for (int i = mItems.size() - 1; i >= 0 && mItems.size() > MAX_ITEMS; i--) {
            if (!mItems.get(i).mPinned) {
                mItems.remove(i);
            }
        }
        while (mItems.size() > MAX_ITEMS) {
            mItems.remove(mItems.size() - 1);
        }
    }
}
