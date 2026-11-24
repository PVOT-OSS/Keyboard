/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary

data class PersonalDictionaryEntry(
    val word: String,
    private val rawFrequency: Int,
    private val rawLocale: String?,
    private val rawShortcut: String?,
) {
    val frequency: Int = rawFrequency.coerceIn(MIN_FREQUENCY, MAX_FREQUENCY)
    val locale: String = rawLocale.orEmpty()
    val shortcut: String? = rawShortcut?.takeIf { it.isNotEmpty() }

    fun toLine(): String =
        listOf(word, frequency.toString(), locale, shortcut.orEmpty()).joinToString(SEPARATOR)

    fun key(): String = word + SEPARATOR + locale

    companion object {
        private const val SEPARATOR = "\t"
        private const val COMMENT_PREFIX = "#"
        private const val MIN_FREQUENCY = 1
        private const val MAX_FREQUENCY = 255
        private const val MAX_LENGTH = 48
        const val DEFAULT_FREQUENCY = 250

        fun fromLine(line: String): PersonalDictionaryEntry? {
            if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
                return null
            }
            val fields = line.split(SEPARATOR)
            val word = fields[0].trim()
            if (word.isEmpty() || word.length > MAX_LENGTH) {
                return null
            }
            val frequencyField = fields.getOrNull(1)?.trim().orEmpty()
            val frequency = if (frequencyField.isEmpty()) {
                DEFAULT_FREQUENCY
            } else {
                frequencyField.toIntOrNull() ?: return null
            }
            val locale = fields.getOrNull(2)?.trim().orEmpty()
            val shortcut = fields.getOrNull(3)?.trim().orEmpty()
            if (shortcut.length > MAX_LENGTH) {
                return null
            }
            return PersonalDictionaryEntry(word, frequency, locale, shortcut)
        }
    }
}
