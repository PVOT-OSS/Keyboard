/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

object ClipKinds {
    enum class Kind { EMAIL, LINK, PHONE }

    private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")
    private val LINK = Regex("^((https?://|www\\.)\\S+|[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}(/\\S*)?)$", RegexOption.IGNORE_CASE)
    private val PHONE = Regex("^\\+?[0-9(][0-9 ()./-]*[0-9]$")
    private const val MIN_PHONE_DIGITS = 7
    private const val MAX_PHONE_DIGITS = 15

    @JvmStatic
    fun kindOf(text: String): Kind? {
        val value = text.trim()
        return when {
            EMAIL.matches(value) -> Kind.EMAIL
            PHONE.matches(value) && value.count(Char::isDigit) in MIN_PHONE_DIGITS..MAX_PHONE_DIGITS ->
                Kind.PHONE
            LINK.matches(value) -> Kind.LINK
            else -> null
        }
    }
}
