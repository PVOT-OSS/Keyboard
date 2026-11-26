/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import android.content.Context
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.prauga.keyboard.AudioAndHapticFeedbackManager
import org.prauga.keyboard.clipboard.ClipboardHistoryManager
import org.prauga.keyboard.personalization.PersonalizationHelper
import org.prauga.keyboard.photo.KeyboardPhoto
import org.prauga.keyboard.stats.TypingStats

internal class DeviceSettingsActions(context: Context) : SettingsActions {

    private val context: Context = context.applicationContext

    override suspend fun removePhoto() =
        withContext(Dispatchers.IO) { KeyboardPhoto.remove(context) }

    override fun clearClipboard() {
        ClipboardHistoryManager.getInstance(context).clearAll()
    }

    override suspend fun clearLearnedWords() =
        withContext(Dispatchers.IO) {
            PersonalizationHelper.removeAllUserHistoryDictionaries(context)
        }

    override suspend fun resetStats() = withContext(Dispatchers.IO) { TypingStats.reset(context) }

    override suspend fun readDocument(path: String): String =
        withContext(Dispatchers.IO) {
            val text = context.assets.open(path).bufferedReader().use { it.readText() }
            text.trim().split(PARAGRAPH_BREAK).joinToString("\n\n") { paragraph ->
                paragraph.lines().joinToString(" ") { it.trim() }
            }
        }

    override fun playKeySound(percent: Int) {
        val audio = context.getSystemService(AudioManager::class.java) ?: return
        audio.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, percent / PERCENT)
    }

    override fun vibrate(milliseconds: Int) {
        AudioAndHapticFeedbackManager.init(context)
        AudioAndHapticFeedbackManager.getInstance().vibrate(milliseconds.toLong())
    }

    private companion object {
        val PARAGRAPH_BREAK = Regex("\\n\\s*\\n")
    }
}
