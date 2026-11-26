/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.textfield

import androidx.annotation.StringRes

interface StripTextFieldClient {
    class Result(val label: String, val description: String, val onPick: () -> Unit)

    @get:StringRes
    val hint: Int

    @get:StringRes
    val noResults: Int

    val initialText: String
        get() = ""

    val largeResults: Boolean
        get() = false

    fun results(text: String): List<Result>

    fun onBack()
}
