/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions

import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.Reader
import java.util.Locale
import org.prauga.keyboard.utils.ExecutorUtils

object EmojiKeywords {
    private const val TAG = "EmojiKeywords"
    private const val ENGLISH_ASSET = "emoji/keywords-en.tsv"
    private const val ENGLISH = "en"
    private const val MAX_SEARCH_RESULTS = 40

    @Volatile private var english = Index(emptyMap())
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
                english =
                    Index(appContext.assets.open(ENGLISH_ASSET).reader(Charsets.UTF_8).use(::parse))
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
        return english.keywords[word.lowercase(Locale.ROOT)]?.firstOrNull()
    }

    @JvmStatic fun search(query: String): List<String> = english.search(query)

    internal fun parse(reader: Reader): Map<String, List<String>> =
        reader
            .buffered()
            .lineSequence()
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val tab = line.indexOf('\t')
                val emojis =
                    if (tab > 0) {
                        line.substring(tab + 1).split(' ').filter { it.isNotEmpty() }
                    } else {
                        emptyList()
                    }
                if (emojis.isEmpty()) null else line.substring(0, tab) to emojis
            }
            .toMap()

    internal class Index(val keywords: Map<String, List<String>>) {
        private val sortedKeywords = keywords.keys.sorted()

        fun search(query: String): List<String> {
            val words = query.lowercase(Locale.ROOT).split(' ').filter { it.isNotEmpty() }
            if (words.isEmpty()) {
                return emptyList()
            }
            val others = words.drop(1).map { matches(it).toSet() }
            return matches(words[0])
                .filter { emoji -> others.all { emoji in it } }
                .take(MAX_SEARCH_RESULTS)
        }

        private fun matches(word: String): List<String> {
            val result = LinkedHashSet<String>()
            keywords[word]?.let(result::addAll)
            val start = sortedKeywords.binarySearch(word).let { if (it < 0) -it - 1 else it }
            for (i in start until sortedKeywords.size) {
                val keyword = sortedKeywords[i]
                if (!keyword.startsWith(word)) {
                    break
                }
                keywords[keyword]?.let(result::addAll)
            }
            return result.toList()
        }
    }
}
