/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.glide

import java.util.Locale
import org.prauga.keyboard.Dictionary
import org.prauga.keyboard.DictionaryCollection
import org.prauga.keyboard.DictionaryFacilitator
import org.prauga.keyboard.ExpandableBinaryDictionary
import org.prauga.keyboard.NgramContext
import org.prauga.keyboard.ReadOnlyBinaryDictionary
import org.prauga.keyboard.SuggestedWords.SuggestedWordInfo
import org.prauga.keyboard.blockedwords.BlockedWords
import org.prauga.keyboard.common.InputPointers
import org.prauga.keyboard.keyboard.Keyboard
import org.prauga.keyboard.makedict.WordProperty
import org.prauga.keyboard.settings.SettingsValuesForSuggestion
import org.prauga.keyboard.utils.ExecutorUtils

object GlideTyping {
    private const val MAX_RESULTS = 18
    private const val SCORE_STEP = 1000
    private const val CONTEXT_WEIGHT = 3f
    private const val MAX_PROBABILITY = 255f
    private const val PERSONAL_FREQUENCY = 200

    @Volatile private var wordList: GlideWordList? = null
    private var loadedSource: Source? = null
    private var loadingSource: Source? = null

    @Volatile private var cache: CachedDecoder? = null

    private class CachedDecoder(
        val keyboard: Keyboard,
        val words: GlideWordList,
        val decoder: GlideDecoder,
    )

    private data class Source(val locale: Locale, val dictionaries: List<Dictionary>)

    @JvmStatic
    fun prepare(facilitator: DictionaryFacilitator) {
        val locale = facilitator.locale
        val main = facilitator.mainDictionary
        if (locale == null || main == null) {
            return
        }
        val personal =
            listOfNotNull(
                facilitator.getSubDictionary(Dictionary.TYPE_USER),
                facilitator.getSubDictionary(Dictionary.TYPE_CONTACTS),
            )
        val source = Source(locale, listOf(main) + facilitator.secondaryMainDictionaries)
        if (!claim(source)) {
            return
        }
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            val loaded = load(source.dictionaries, personal, locale)
            synchronized(this) {
                if (loadingSource == source) {
                    loadingSource = null
                    if (loaded.size > 0) {
                        wordList = loaded
                        loadedSource = source
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
        facilitator: DictionaryFacilitator,
        ngramContext: NgramContext,
    ): ArrayList<SuggestedWordInfo>? {
        val words = wordList?.takeIf { it.locale == facilitator.locale } ?: return null
        prepare(facilitator)
        val decoded =
            decoderFor(keyboard, words).decode(
                pointers.xCoordinates,
                pointers.yCoordinates,
                pointers.pointerSize,
                MAX_RESULTS,
            ) { word, offensive ->
                !(offensive && settings.mBlockPotentiallyOffensive) &&
                    !BlockedWords.isBlocked(settings.mBlockedWords, word)
            }
        val main = facilitator.mainDictionary
        val results =
            if (main == null || !ngramContext.isValid) {
                decoded
            } else {
                decoded
                    .map { it.copy(score = it.score + contextBonus(main, ngramContext, it.word)) }
                    .sortedByDescending { it.score }
            }
        val suggestions = ArrayList<SuggestedWordInfo>(results.size)
        results.forEachIndexed { rank, candidate ->
            suggestions.add(
                SuggestedWordInfo(
                    candidate.word,
                    "",
                    (results.size - rank) * SCORE_STEP,
                    SuggestedWordInfo.KIND_CORRECTION,
                    Dictionary.DICTIONARY_HARDCODED,
                    SuggestedWordInfo.NOT_AN_INDEX,
                    SuggestedWordInfo.NOT_A_CONFIDENCE,
                )
            )
        }
        return suggestions
    }

    private fun contextBonus(main: Dictionary, ngramContext: NgramContext, word: String): Float {
        val dictionaries = (main as? DictionaryCollection)?.dictionaries ?: listOf(main)
        var best = Dictionary.NOT_A_PROBABILITY
        for (dictionary in dictionaries) {
            if (dictionary is ReadOnlyBinaryDictionary) {
                best = maxOf(best, dictionary.getNgramProbability(ngramContext, word))
            }
        }
        return if (best == Dictionary.NOT_A_PROBABILITY) 0f
        else CONTEXT_WEIGHT * best / MAX_PROBABILITY
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
        val decoder =
            GlideDecoder(GlideKeyLayout(centers, keyboard.mMostCommonKeyWidth.toFloat()), words)
        cache = CachedDecoder(keyboard, words, decoder)
        return decoder
    }

    private fun load(
        sources: List<Dictionary>,
        personal: List<ExpandableBinaryDictionary>,
        locale: Locale,
    ): GlideWordList {
        val dictionaries = sources.flatMap {
            (it as? DictionaryCollection)?.dictionaries ?: listOf(it)
        }
        val frequencies = HashMap<String, Int>()
        val offensive = HashSet<String>()
        dictionaries.filterIsInstance<ReadOnlyBinaryDictionary>().forEach {
            addWords(it, frequencies, offensive)
        }
        for (dictionary in personal) {
            dictionary.wordPropertiesForSyncing.orEmpty().filter(::isGlideWord).forEach {
                frequencies[it.mWord] = maxOf(frequencies[it.mWord] ?: -1, PERSONAL_FREQUENCY)
            }
        }
        val words = frequencies.keys.toTypedArray()
        return GlideWordList(
            locale,
            words,
            IntArray(words.size) { frequencies.getValue(words[it]) },
            BooleanArray(words.size) { words[it] in offensive },
        )
    }

    private fun claim(source: Source): Boolean =
        synchronized(this) {
            val free = source != loadedSource && source != loadingSource
            if (free) {
                loadingSource = source
            }
            free
        }

    private fun addWords(
        dictionary: ReadOnlyBinaryDictionary,
        frequencies: MutableMap<String, Int>,
        offensive: MutableSet<String>,
    ) {
        var token = 0
        do {
            val result = dictionary.getNextWordProperty(token)
            val property = result?.mWordProperty
            if (property != null && isGlideWord(property)) {
                val word = property.mWord
                val frequency = property.mProbabilityInfo.mProbability
                if (frequency > (frequencies[word] ?: -1)) {
                    frequencies[word] = frequency
                }
                if (property.mIsPossiblyOffensive) {
                    offensive.add(word)
                }
            }
            token = if (property == null) 0 else result.mNextToken
        } while (token != 0)
    }

    private fun isGlideWord(property: WordProperty): Boolean =
        !property.mIsNotAWord && property.mWord.isNotEmpty() && ' ' !in property.mWord
}
