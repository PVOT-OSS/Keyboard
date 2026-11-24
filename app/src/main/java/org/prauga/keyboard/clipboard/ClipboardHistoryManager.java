/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.prauga.keyboard.utils.ExecutorUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ClipboardHistoryManager {
    private static final String TAG = ClipboardHistoryManager.class.getSimpleName();
    private static final String PINS_FILE_NAME = "clipboard_pins";

    @Nullable
    private static ClipboardHistoryManager sInstance;

    private final ClipboardHistory mHistory = new ClipboardHistory();
    private final File mPinsFile;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Nonnull
    public static synchronized ClipboardHistoryManager getInstance(
            @Nonnull final Context context) {
        if (sInstance == null) {
            sInstance = new ClipboardHistoryManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private ClipboardHistoryManager(@Nonnull final Context context) {
        mPinsFile = new File(context.getNoBackupFilesDir(), PINS_FILE_NAME);
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute(() -> {
            try {
                final List<ClipboardHistory.Item> pinned = ClipboardHistoryStore.read(mPinsFile);
                mMainHandler.post(() -> mHistory.restorePinned(pinned));
            } catch (final IOException e) {
                Log.w(TAG, "Cannot read pinned clips", e);
            }
        });
    }

    public void add(@Nullable final CharSequence text, final long timestamp,
            final boolean isSensitive) {
        mHistory.add(text, timestamp, isSensitive, System.currentTimeMillis());
    }

    @Nonnull
    public List<ClipboardHistory.Item> getItems() {
        return mHistory.getItems(System.currentTimeMillis());
    }

    public void setPinned(@Nonnull final String text, final boolean pinned) {
        mHistory.setPinned(text, pinned);
        savePins();
    }

    public void remove(@Nonnull final String text) {
        mHistory.remove(text);
        savePins();
    }

    public void clearUnpinned() {
        mHistory.clearUnpinned();
    }

    public void clearAll() {
        mHistory.clearAll();
        savePins();
    }

    private void savePins() {
        final List<ClipboardHistory.Item> pinned = mHistory.getPinnedItems();
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute(() -> {
            try {
                if (pinned.isEmpty()) {
                    if (mPinsFile.exists() && !mPinsFile.delete()) {
                        Log.w(TAG, "Cannot delete pinned clips");
                    }
                } else {
                    ClipboardHistoryStore.write(mPinsFile, pinned);
                }
            } catch (final IOException e) {
                Log.w(TAG, "Cannot save pinned clips", e);
            }
        });
    }
}
