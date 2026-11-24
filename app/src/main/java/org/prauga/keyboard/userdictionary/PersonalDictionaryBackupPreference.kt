/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference

class PersonalDictionaryBackupPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {
    init {
        isPersistent = false
    }

    override fun onClick() {
        val action = if (key == KEY_EXPORT) {
            PersonalDictionaryBackupActivity.ACTION_EXPORT
        } else {
            PersonalDictionaryBackupActivity.ACTION_IMPORT
        }
        context.startActivity(
            Intent(context, PersonalDictionaryBackupActivity::class.java).setAction(action)
        )
    }

    private companion object {
        const val KEY_EXPORT = "export_personal_dictionary"
    }
}
