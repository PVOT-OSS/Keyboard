/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.io.IOException
import org.prauga.keyboard.utils.ExecutorUtils

class ClipboardHistoryManager private constructor(private val context: Context) {
    private val history = ClipboardHistory()
    private val pinsFile = File(context.noBackupFilesDir, PINS_FILE_NAME)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastImageNames: Set<String>? = null
    private var pinsLoaded = false

    init {
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            try {
                val pinned = ClipboardHistoryStore.read(pinsFile)
                mainHandler.post {
                    synchronized(this) {
                        history.restorePinned(pinned)
                        pinsLoaded = true
                    }
                }
            } catch (e: IOException) {
                Log.w(TAG, "Cannot read pinned clips", e)
                mainHandler.post { synchronized(this) { pinsLoaded = true } }
            }
        }
    }

    @Synchronized
    fun add(text: CharSequence?, timestamp: Long, isSensitive: Boolean) {
        history.add(text, timestamp, isSensitive, System.currentTimeMillis())
    }

    @Synchronized
    fun getItems(): List<ClipboardHistory.Item> {
        val items = history.getItems(System.currentTimeMillis())
        deleteUnusedImages()
        return items
    }

    @Synchronized
    fun addImage(name: String, mimeType: String, timestamp: Long) {
        history.add(name, timestamp, false, System.currentTimeMillis(), mimeType)
        ClipImages.markAdded(name)
        deleteUnusedImages()
    }

    @Synchronized
    fun setPinned(text: String, pinned: Boolean) {
        history.setPinned(text, pinned)
        savePins()
    }

    @Synchronized
    fun remove(text: String) {
        history.remove(text)
        savePins()
        deleteUnusedImages()
    }

    @Synchronized
    fun setShortcut(text: String, shortcut: String?) {
        history.setShortcut(text, shortcut)
        savePins()
    }

    @Synchronized fun findShortcutText(word: String): String? = history.findByShortcut(word)

    @Synchronized
    fun moveToTop(text: String) {
        history.moveToTop(text, System.currentTimeMillis())
        savePins()
    }

    @Synchronized
    fun replace(oldText: String, newText: String) {
        if (history.replace(oldText, newText, System.currentTimeMillis())) {
            savePins()
        }
    }

    @Synchronized
    fun addClip(text: String) {
        if (history.addManual(text, System.currentTimeMillis())) {
            savePins()
        }
    }

    @Synchronized
    fun clearUnpinned() {
        history.clearUnpinned()
        deleteUnusedImages()
    }

    @Synchronized
    fun clearAll() {
        history.clearAll()
        savePins()
        deleteUnusedImages()
    }

    private fun deleteUnusedImages() {
        if (!pinsLoaded) {
            return
        }
        val names = history.imageNames()
        if (names == lastImageNames) {
            return
        }
        lastImageNames = names
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            ClipImages.deleteUnreferenced(context, names)
        }
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

        @Volatile private var instance: ClipboardHistoryManager? = null

        @JvmStatic
        fun findShortcutTextIfLoaded(word: String): String? = instance?.findShortcutText(word)

        @JvmStatic
        fun getInstance(context: Context): ClipboardHistoryManager =
            instance
                ?: synchronized(this) {
                    instance
                        ?: ClipboardHistoryManager(context.applicationContext).also {
                            instance = it
                        }
                }
    }
}
