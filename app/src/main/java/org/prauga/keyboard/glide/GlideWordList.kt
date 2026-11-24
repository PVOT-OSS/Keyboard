/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.glide

import java.util.Locale

class GlideWordList(
    val locale: Locale,
    val words: Array<String>,
    val frequencies: IntArray,
    val offensive: BooleanArray,
) {
    val size: Int
        get() = words.size
}
