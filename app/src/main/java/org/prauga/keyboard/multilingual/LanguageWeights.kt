/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.multilingual

import java.util.Locale

class LanguageWeights {
    private val scores = HashMap<Locale, Double>()

    fun record(onlyKnownBy: Collection<Locale>) {
        for (entry in scores.entries) {
            entry.setValue(entry.value * DECAY)
        }
        for (locale in onlyKnownBy) {
            scores[locale] = (scores[locale] ?: 0.0) + 1.0
        }
    }

    fun weight(locale: Locale): Float {
        val score = scores[locale] ?: 0.0
        return (MIN_WEIGHT + (1.0 - MIN_WEIGHT) * score / (score + PRIOR)).toFloat()
    }

    private companion object {
        const val DECAY = 0.97
        const val PRIOR = 3.0
        const val MIN_WEIGHT = 0.85
    }
}
