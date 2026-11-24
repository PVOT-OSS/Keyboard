/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.glide

class GlideKeyLayout(centers: Map<Int, Pair<Float, Float>>, val keyWidth: Float) {
    private val codes = centers.keys.toIntArray()
    private val xs = FloatArray(codes.size) { centers.getValue(codes[it]).first }
    private val ys = FloatArray(codes.size) { centers.getValue(codes[it]).second }
    private val indexByCode = HashMap<Int, Int>().apply { codes.forEachIndexed { i, c -> put(c, i) } }

    val keyCount: Int
        get() = codes.size

    fun keyIndexOf(codePoint: Int): Int = indexByCode[codePoint] ?: -1

    fun x(keyIndex: Int): Float = xs[keyIndex]

    fun y(keyIndex: Int): Float = ys[keyIndex]

    fun keysNear(x: Float, y: Float, radius: Float): IntArray {
        var nearest = -1
        var nearestDistance = Float.MAX_VALUE
        val result = ArrayList<Int>()
        for (i in codes.indices) {
            val d = distance(x, y, xs[i], ys[i])
            if (d <= radius) {
                result.add(i)
            }
            if (d < nearestDistance) {
                nearestDistance = d
                nearest = i
            }
        }
        if (result.isEmpty() && nearest >= 0) {
            result.add(nearest)
        }
        return result.toIntArray()
    }
}

internal fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return kotlin.math.sqrt(dx * dx + dy * dy)
}
