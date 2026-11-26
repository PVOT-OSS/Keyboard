/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.glide

import java.util.Locale
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlideDecoderTest {
    private val layout = qwerty()
    private val words =
        listOf(
            "hello" to 200,
            "help" to 190,
            "hell" to 150,
            "jello" to 60,
            "hollow" to 90,
            "holy" to 120,
            "hole" to 130,
            "world" to 200,
            "word" to 190,
            "would" to 210,
            "the" to 255,
            "they" to 220,
            "then" to 215,
            "them" to 210,
            "there" to 215,
            "quick" to 140,
            "quiz" to 100,
            "keyboard" to 120,
            "keyword" to 90,
            "I'm" to 200,
        )
    private val decoder =
        GlideDecoder(
            layout,
            GlideWordList(
                Locale.ENGLISH,
                words.map { it.first }.toTypedArray(),
                words.map { it.second }.toIntArray(),
                BooleanArray(words.size),
            ),
        )

    @Test
    fun cleanSwipesFindTheirWord() {
        for ((word, _) in words) {
            assertEquals(word, top(swipe(word, noise = 0f, seed = 1)))
        }
    }

    @Test
    fun noisySwipesStillFindTheirWord() {
        var hits = 0
        var total = 0
        for ((word, _) in words) {
            for (seed in 1..5) {
                total++
                if (top(swipe(word, noise = 18f, seed = seed)) == word) {
                    hits++
                }
            }
        }
        assertTrue("$hits of $total", hits >= total * 9 / 10)
    }

    @Test
    fun rejectedWordsAreSkipped() {
        val path = swipe("hello", noise = 0f, seed = 1)
        val results =
            decoder.decode(path.first, path.second, path.first.size, 5) { w, _ -> w != "hello" }
        assertTrue(results.none { it.word == "hello" })
    }

    @Test
    fun aTapIsNotAGesture() {
        assertTrue(decoder.decode(intArrayOf(10), intArrayOf(10), 1, 5) { _, _ -> true }.isEmpty())
    }

    private fun top(path: Pair<IntArray, IntArray>): String? =
        decoder
            .decode(path.first, path.second, path.first.size, 5) { _, _ -> true }
            .firstOrNull()
            ?.word

    private fun swipe(word: String, noise: Float, seed: Int): Pair<IntArray, IntArray> {
        val random = Random(seed)
        val xs = ArrayList<Int>()
        val ys = ArrayList<Int>()
        val keys =
            word
                .lowercase(Locale.ROOT)
                .filter { layout.keyIndexOf(it.code) >= 0 }
                .map { layout.keyIndexOf(it.code) }
        for (k in keys.indices) {
            val x1 = layout.x(keys[k])
            val y1 = layout.y(keys[k])
            if (k == 0) {
                xs.add(x1.toInt())
                ys.add(y1.toInt())
                continue
            }
            val x0 = layout.x(keys[k - 1])
            val y0 = layout.y(keys[k - 1])
            for (step in 1..8) {
                val t = step / 8f
                xs.add((x0 + (x1 - x0) * t + (random.nextFloat() * 2 - 1) * noise).toInt())
                ys.add((y0 + (y1 - y0) * t + (random.nextFloat() * 2 - 1) * noise).toInt())
            }
        }
        return xs.toIntArray() to ys.toIntArray()
    }

    private fun qwerty(): GlideKeyLayout {
        val rows = listOf("qwertyuiop" to 0f, "asdfghjkl" to 50f, "zxcvbnm" to 150f)
        val centers = HashMap<Int, Pair<Float, Float>>()
        rows.forEachIndexed { row, (letters, offset) ->
            letters.forEachIndexed { i, c ->
                centers[c.code] = (offset + i * 100f + 50f) to (row * 150f + 75f)
            }
        }
        return GlideKeyLayout(centers, 100f)
    }
}
