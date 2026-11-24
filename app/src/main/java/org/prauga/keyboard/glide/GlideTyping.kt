/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.glide

import org.prauga.keyboard.Dictionary
import org.prauga.keyboard.DictionaryCollection
import org.prauga.keyboard.DictionaryFacilitator
import org.prauga.keyboard.ReadOnlyBinaryDictionary
import org.prauga.keyboard.SuggestedWords.SuggestedWordInfo
import org.prauga.keyboard.blockedwords.BlockedWords
import org.prauga.keyboard.common.InputPointers
import org.prauga.keyboard.keyboard.Keyboard
import org.prauga.keyboard.settings.SettingsValuesForSuggestion
import org.prauga.keyboard.utils.ExecutorUtils
import java.util.Locale

object GlideTyping {
    private const val MAX_RESULTS = 18
    private const val SCORE_STEP = 1000

    @Volatile
    private var wordList: GlideWordList? = null
    private var loadingLocale: Locale? = null

    @Volatile
    private var cache: CachedDecoder? = null

    private class CachedDecoder(val keyboard: Keyboard, val words: GlideWordList, val decoder: GlideDecoder)

    @JvmStatic
    fun prepare(facilitator: DictionaryFacilitator) {
        val locale = facilitator.locale ?: return
        val main = facilitator.mainDictionary ?: return
        synchronized(this) {
            if (wordList?.locale == locale || loadingLocale == locale) {
                return
            }
            loadingLocale = locale
        }
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            val loaded = load(main, locale)
            synchronized(this) {
                if (loadingLocale == locale) {
                    loadingLocale = null
                    if (loaded.size > 0) {
                        wordList = loaded
                    }
                }
            }
        }
    }

    @JvmStatic
    fun suggest(
        pointers: InputPointers,
        keyboard: Keyboard,
        settings: SettingsValuesForSuggestion,
        locale: Locale?,
    ): ArrayList<SuggestedWordInfo>? {
        val words = wordList?.takeIf { it.locale == locale } ?: return null
        val results = decoderFor(keyboard, words).decode(
            pointers.xCoordinates, pointers.yCoordinates, pointers.pointerSize, MAX_RESULTS
        ) { word, offensive ->
            !(offensive && settings.mBlockPotentiallyOffensive) &&
                !BlockedWords.isBlocked(settings.mBlockedWords, word)
        }
        val suggestions = ArrayList<SuggestedWordInfo>(results.size)
        results.forEachIndexed { rank, candidate ->
            suggestions.add(
                SuggestedWordInfo(
                    candidate.word, "", (results.size - rank) * SCORE_STEP,
                    SuggestedWordInfo.KIND_CORRECTION, Dictionary.DICTIONARY_HARDCODED,
                    SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE
                )
            )
        }
        return suggestions
    }

    private fun decoderFor(keyboard: Keyboard, words: GlideWordList): GlideDecoder {
        cache?.let {
            if (it.keyboard === keyboard && it.words === words) {
                return it.decoder
            }
        }
        val centers = HashMap<Int, Pair<Float, Float>>()
        for (key in keyboard.sortedKeys) {
            val code = key.code
            if (code > 0 && (Character.isLetter(code) || code == '\''.code)) {
                centers[Character.toLowerCase(code)] =
                    (key.x + key.width / 2f) to (key.y + key.height / 2f)
            }
        }
        val decoder = GlideDecoder(GlideKeyLayout(centers, keyboard.mMostCommonKeyWidth.toFloat()), words)
        cache = CachedDecoder(keyboard, words, decoder)
        return decoder
    }

    private fun load(main: Dictionary, locale: Locale): GlideWordList {
        val dictionaries = (main as? DictionaryCollection)?.dictionaries ?: listOf(main)
        val frequencies = HashMap<String, Int>()
        val offensive = HashSet<String>()
        for (dictionary in dictionaries) {
            if (dictionary !is ReadOnlyBinaryDictionary) {
                continue
            }
            var token = 0
            do {
                val result = dictionary.getNextWordProperty(token) ?: break
                val property = result.mWordProperty ?: break
                val word = property.mWord
                if (!property.mIsNotAWord && word.isNotEmpty() && ' ' !in word) {
                    val frequency = property.mProbabilityInfo.mProbability
                    if (frequency > (frequencies[word] ?: -1)) {
                        frequencies[word] = frequency
                    }
                    if (property.mIsPossiblyOffensive) {
                        offensive.add(word)
                    }
                }
                token = result.mNextToken
            } while (token != 0)
        }
        val words = frequencies.keys.toTypedArray()
        return GlideWordList(
            locale,
            words,
            IntArray(words.size) { frequencies.getValue(words[it]) },
            BooleanArray(words.size) { words[it] in offensive },
        )
    }
}
