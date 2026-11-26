/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

internal interface SettingsActions {
    suspend fun removePhoto()

    fun clearClipboard()

    suspend fun clearLearnedWords()

    suspend fun resetStats()

    suspend fun readDocument(path: String): String

    fun playKeySound(percent: Int)

    fun vibrate(milliseconds: Int)
}
