/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.glide

import java.util.Locale
import java.util.PriorityQueue
import kotlin.math.max
import kotlin.math.min

class GlideDecoder(private val layout: GlideKeyLayout, private val wordList: GlideWordList) {
    data class Candidate(val word: String, val score: Float)

    private val index = HashMap<Int, IntArray>()

    init {
        val buckets = HashMap<Int, ArrayList<Int>>()
        for (i in 0 until wordList.size) {
            val keys = keyPath(wordList.words[i])
            if (keys.isEmpty()) {
                continue
            }
            buckets.getOrPut(bucket(keys.first(), keys.last())) { ArrayList() }.add(i)
        }
        for ((key, ids) in buckets) {
            index[key] = ids.toIntArray()
        }
    }

    fun decode(
        xs: IntArray,
        ys: IntArray,
        size: Int,
        maxResults: Int,
        accept: (String, Boolean) -> Boolean,
    ): List<Candidate> {
        if (size < 2) {
            return emptyList()
        }
        val rawX = FloatArray(size) { xs[it].toFloat() }
        val rawY = FloatArray(size) { ys[it].toFloat() }
        val input = resample(rawX, rawY)
        val inputLength = pathLength(rawX, rawY)
        val normalizedInput = normalize(input)
        val radius = layout.keyWidth * PRUNE_RADIUS
        val starts = layout.keysNear(input.x[0], input.y[0], radius)
        val ends = layout.keysNear(input.x[SAMPLES - 1], input.y[SAMPLES - 1], radius)
        val best = PriorityQueue<Candidate>(maxResults + 1, compareBy { it.score })
        for (start in starts) {
            for (end in ends) {
                val ids = index[bucket(start, end)] ?: continue
                for (id in ids) {
                    val word = wordList.words[id]
                    if (!accept(word, wordList.offensive[id])) {
                        continue
                    }
                    val score = score(word, wordList.frequencies[id], input, normalizedInput, inputLength)
                        ?: continue
                    val worst = best.peek()
                    if (best.size < maxResults) {
                        best.add(Candidate(word, score))
                    } else if (worst != null && score > worst.score) {
                        best.poll()
                        best.add(Candidate(word, score))
                    }
                }
            }
        }
        return best.sortedByDescending { it.score }
    }

    private fun score(
        word: String,
        frequency: Int,
        input: Path,
        normalizedInput: Path,
        inputLength: Float,
    ): Float? {
        val keys = keyPath(word)
        if (keys.isEmpty()) {
            return null
        }
        val rawX = FloatArray(keys.size) { layout.x(keys[it]) }
        val rawY = FloatArray(keys.size) { layout.y(keys[it]) }
        val idealLength = pathLength(rawX, rawY)
        val longest = max(idealLength, inputLength)
        if (longest > layout.keyWidth * 2 &&
            min(idealLength, inputLength) / longest < MIN_LENGTH_RATIO
        ) {
            return null
        }
        val ideal = resample(rawX, rawY)
        val shape = meanDistance(normalizedInput, normalize(ideal))
        val location = meanDistance(input, ideal) / layout.keyWidth
        val last = SAMPLES - 1
        val ends = (distance(input.x[0], input.y[0], ideal.x[0], ideal.y[0]) +
            distance(input.x[last], input.y[last], ideal.x[last], ideal.y[last])) /
            (2 * layout.keyWidth)
        val shapeTerm = shape / SHAPE_SIGMA
        val locationTerm = location / LOCATION_SIGMA
        val endsTerm = ends / ENDS_SIGMA
        return -(shapeTerm * shapeTerm) - (locationTerm * locationTerm) - (endsTerm * endsTerm) +
            FREQUENCY_WEIGHT * frequency / MAX_FREQUENCY
    }

    private fun keyPath(word: String): IntArray {
        val keys = ArrayList<Int>(word.length)
        var i = 0
        val lower = word.lowercase(Locale.ROOT)
        while (i < lower.length) {
            val codePoint = lower.codePointAt(i)
            i += Character.charCount(codePoint)
            val key = layout.keyIndexOf(codePoint)
            if (key < 0) {
                if (Character.isLetter(codePoint)) {
                    return IntArray(0)
                }
                continue
            }
            if (keys.isEmpty() || keys.last() != key) {
                keys.add(key)
            }
        }
        return keys.toIntArray()
    }

    private fun bucket(first: Int, last: Int) = first * layout.keyCount + last

    internal class Path(val x: FloatArray, val y: FloatArray)

    internal companion object {
        const val SAMPLES = 40
        const val PRUNE_RADIUS = 1.1f
        const val MIN_LENGTH_RATIO = 0.4f
        const val SHAPE_SIGMA = 0.12f
        const val LOCATION_SIGMA = 0.45f
        const val ENDS_SIGMA = 0.35f
        const val FREQUENCY_WEIGHT = 2.5f
        const val MAX_FREQUENCY = 255f

        fun pathLength(xs: FloatArray, ys: FloatArray): Float {
            var length = 0f
            for (i in 1 until xs.size) {
                length += distance(xs[i - 1], ys[i - 1], xs[i], ys[i])
            }
            return length
        }

        fun resample(xs: FloatArray, ys: FloatArray): Path {
            val outX = FloatArray(SAMPLES)
            val outY = FloatArray(SAMPLES)
            val total = pathLength(xs, ys)
            if (xs.size == 1 || total == 0f) {
                outX.fill(xs[0])
                outY.fill(ys[0])
                return Path(outX, outY)
            }
            val step = total / (SAMPLES - 1)
            outX[0] = xs[0]
            outY[0] = ys[0]
            var count = 1
            var walked = 0f
            var target = step
            for (i in 1 until xs.size) {
                val segment = distance(xs[i - 1], ys[i - 1], xs[i], ys[i])
                while (segment > 0f && walked + segment >= target && count < SAMPLES - 1) {
                    val t = (target - walked) / segment
                    outX[count] = xs[i - 1] + t * (xs[i] - xs[i - 1])
                    outY[count] = ys[i - 1] + t * (ys[i] - ys[i - 1])
                    count++
                    target += step
                }
                walked += segment
            }
            while (count < SAMPLES) {
                outX[count] = xs[xs.size - 1]
                outY[count] = ys[ys.size - 1]
                count++
            }
            return Path(outX, outY)
        }

        fun normalize(path: Path): Path {
            val minX = path.x.min()
            val maxX = path.x.max()
            val minY = path.y.min()
            val maxY = path.y.max()
            val scale = max(maxX - minX, maxY - minY).let { if (it < 1f) 1f else it }
            val cx = path.x.average().toFloat()
            val cy = path.y.average().toFloat()
            return Path(
                FloatArray(path.x.size) { (path.x[it] - cx) / scale },
                FloatArray(path.y.size) { (path.y[it] - cy) / scale },
            )
        }

        fun meanDistance(a: Path, b: Path): Float {
            var sum = 0f
            for (i in a.x.indices) {
                sum += distance(a.x[i], a.y[i], b.x[i], b.y[i])
            }
            return sum / a.x.size
        }
    }
}
