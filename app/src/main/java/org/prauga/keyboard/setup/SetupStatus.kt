/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup

import android.content.Context
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.prauga.keyboard.utils.UncachedInputMethodManagerUtils

internal enum class SetupStep {
    ENABLE,
    SWITCH,
    DONE,
}

internal class SetupStatus(context: Context) {

    private val context: Context = context.applicationContext
    private val imm: InputMethodManager =
        requireNotNull(this.context.getSystemService(InputMethodManager::class.java)) {
            "No InputMethodManager"
        }

    suspend fun imeId(): String? =
        withContext(Dispatchers.IO) {
            UncachedInputMethodManagerUtils.getInputMethodInfoOf(context.packageName, imm)?.id
        }

    suspend fun current(): SetupStep =
        withContext(Dispatchers.IO) {
            when {
                !UncachedInputMethodManagerUtils.isThisImeEnabled(context, imm) -> SetupStep.ENABLE
                !UncachedInputMethodManagerUtils.isThisImeCurrent(context, imm) -> SetupStep.SWITCH
                else -> SetupStep.DONE
            }
        }
}
