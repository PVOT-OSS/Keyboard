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
        private const val FIELD_WORD = 0
        private const val FIELD_FREQUENCY = 1
        private const val FIELD_LOCALE = 2
        private const val FIELD_SHORTCUT = 3
        const val DEFAULT_FREQUENCY = 250

        fun fromLine(line: String): PersonalDictionaryEntry? {
            if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
                return null
            }
            val fields = line.split(SEPARATOR).map { it.trim() }
            val word = fields[FIELD_WORD]
            val frequency = parseFrequency(fields.getOrNull(FIELD_FREQUENCY).orEmpty())
            val shortcut = fields.getOrNull(FIELD_SHORTCUT).orEmpty()
            val valid =
                word.isNotEmpty() && word.length <= MAX_LENGTH && shortcut.length <= MAX_LENGTH
            return if (valid && frequency != null) {
                PersonalDictionaryEntry(
                    word,
                    frequency,
                    fields.getOrNull(FIELD_LOCALE).orEmpty(),
                    shortcut,
                )
            } else {
                null
            }
        }

        private fun parseFrequency(field: String): Int? =
            if (field.isEmpty()) DEFAULT_FREQUENCY else field.toIntOrNull()
    }
}
