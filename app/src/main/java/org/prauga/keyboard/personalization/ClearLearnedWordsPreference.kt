/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.personalization

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.prauga.keyboard.R
import org.prauga.keyboard.utils.ExecutorUtils

class ClearLearnedWordsPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {
    init {
        isPersistent = false
    }

    override fun onClick() {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.clear_learned_words_confirm_title)
            .setMessage(R.string.clear_learned_words_confirm_message)
            .setPositiveButton(R.string.clear_learned_words_confirm) { _, _ -> clearLearnedWords() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun clearLearnedWords() {
        val appContext = context.applicationContext
        val mainHandler = Handler(Looper.getMainLooper())
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            PersonalizationHelper.removeAllUserHistoryDictionaries(appContext)
            mainHandler.post {
                Toast.makeText(appContext, R.string.clear_learned_words_done, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
