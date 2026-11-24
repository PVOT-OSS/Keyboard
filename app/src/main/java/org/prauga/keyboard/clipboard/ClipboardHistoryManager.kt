/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.prauga.keyboard.utils.ExecutorUtils
import java.io.File
import java.io.IOException

class ClipboardHistoryManager private constructor(context: Context) {
    private val history = ClipboardHistory()
    private val pinsFile = File(context.noBackupFilesDir, PINS_FILE_NAME)
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            try {
                val pinned = ClipboardHistoryStore.read(pinsFile)
                mainHandler.post { history.restorePinned(pinned) }
            } catch (e: IOException) {
                Log.w(TAG, "Cannot read pinned clips", e)
            }
        }
    }

    fun add(text: CharSequence?, timestamp: Long, isSensitive: Boolean) {
        history.add(text, timestamp, isSensitive, System.currentTimeMillis())
    }

    fun getItems(): List<ClipboardHistory.Item> = history.getItems(System.currentTimeMillis())

    fun setPinned(text: String, pinned: Boolean) {
        history.setPinned(text, pinned)
        savePins()
    }

    fun remove(text: String) {
        history.remove(text)
        savePins()
    }

    fun clearUnpinned() {
        history.clearUnpinned()
    }

    fun clearAll() {
        history.clearAll()
        savePins()
    }

    private fun savePins() {
        val pinned = history.getPinnedItems()
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            try {
                if (pinned.isEmpty()) {
                    if (pinsFile.exists() && !pinsFile.delete()) {
                        Log.w(TAG, "Cannot delete pinned clips")
                    }
                } else {
                    ClipboardHistoryStore.write(pinsFile, pinned)
                }
            } catch (e: IOException) {
                Log.w(TAG, "Cannot save pinned clips", e)
            }
        }
    }

    companion object {
        private const val TAG = "ClipboardHistoryManager"
        private const val PINS_FILE_NAME = "clipboard_pins"

        @Volatile
        private var instance: ClipboardHistoryManager? = null

        @JvmStatic
        fun getInstance(context: Context): ClipboardHistoryManager =
            instance ?: synchronized(this) {
                instance ?: ClipboardHistoryManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
