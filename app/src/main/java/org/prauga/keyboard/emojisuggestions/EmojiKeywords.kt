/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions

import android.content.Context
import android.util.Log
import org.prauga.keyboard.utils.ExecutorUtils
import java.io.IOException
import java.io.Reader
import java.util.Locale

object EmojiKeywords {
    private const val TAG = "EmojiKeywords"
    private const val ENGLISH_ASSET = "emoji/keywords-en.tsv"
    private const val ENGLISH = "en"

    @Volatile
    private var english: Map<String, String> = emptyMap()
    private var loading = false

    @JvmStatic
    @Synchronized
    fun load(context: Context) {
        if (loading) {
            return
        }
        loading = true
        val appContext = context.applicationContext
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            try {
                english = appContext.assets.open(ENGLISH_ASSET).reader(Charsets.UTF_8).use(::parse)
            } catch (e: IOException) {
                Log.w(TAG, "Cannot read emoji keywords", e)
            }
        }
    }

    @JvmStatic
    fun lookup(word: String, locale: Locale?): String? {
        if (locale?.language != ENGLISH) {
            return null
        }
        return english[word.lowercase(Locale.ROOT)]
    }

    internal fun parse(reader: Reader): Map<String, String> =
        reader.buffered().lineSequence()
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val tab = line.indexOf('\t')
                if (tab > 0 && tab < line.length - 1) {
                    line.substring(0, tab) to line.substring(tab + 1)
                } else {
                    null
                }
            }
            .toMap()
}
