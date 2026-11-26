/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodSubtype
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.prauga.keyboard.R
import org.prauga.keyboard.RichInputMethodManager
import org.prauga.keyboard.compat.InputMethodSubtypeCompatUtils
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.utils.AdditionalSubtypeUtils
import org.prauga.keyboard.utils.SubtypeLocaleUtils

internal class SystemInputStylesRepository(context: Context) : InputStylesRepository {

    private val context: Context = context.applicationContext
    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this.context)

    override suspend fun styleChoices(): StyleChoices =
        withContext(Dispatchers.IO) {
            RichInputMethodManager.init(context)
            SubtypeLocaleUtils.init(context)
            val imi = RichInputMethodManager.getInstance().inputMethodInfoOfThisIme
            val locales =
                (0 until imi.subtypeCount)
                    .map { imi.getSubtypeAt(it) }
                    .filter { InputMethodSubtypeCompatUtils.isAsciiCapable(it) }
                    .map { localeOf(it) }
                    .distinct()
                    .sorted()
                    .map {
                        Choice(it, SubtypeLocaleUtils.getSubtypeLocaleDisplayNameInSystemLocale(it))
                    }
            val layouts =
                context.resources.getStringArray(R.array.predefined_layouts).map {
                    Choice(it, SubtypeLocaleUtils.getKeyboardLayoutSetDisplayName(it))
                }
            StyleChoices(locales, layouts)
        }

    override suspend fun addStyle(locale: String, layout: String): AddStyleResult =
        withContext(Dispatchers.IO) {
            RichInputMethodManager.init(context)
            val rich = RichInputMethodManager.getInstance()
            val subtype =
                AdditionalSubtypeUtils.createAsciiEmojiCapableAdditionalSubtype(locale, layout)
            if (rich.findSubtypeByLocaleAndKeyboardLayoutSet(locale, layout) != null) {
                return@withContext AddStyleResult.Exists(
                    SubtypeLocaleUtils.getSubtypeDisplayNameInSystemLocale(subtype)
                )
            }
            writeStyles(currentStyles() + subtype)
            AddStyleResult.Added(rich.inputMethodIdOfThisIme)
        }

    override suspend fun removeStyle(prefSubtype: String) =
        withContext(Dispatchers.IO) {
            RichInputMethodManager.init(context)
            writeStyles(
                currentStyles().filter { AdditionalSubtypeUtils.getPrefSubtype(it) != prefSubtype }
            )
        }

    override fun customStyles(): List<CustomStyle> =
        currentStyles().map {
            CustomStyle(
                AdditionalSubtypeUtils.getPrefSubtype(it),
                SubtypeLocaleUtils.getSubtypeDisplayNameInSystemLocale(it),
            )
        }

    private fun currentStyles(): List<InputMethodSubtype> =
        AdditionalSubtypeUtils.createAdditionalSubtypesArray(
                Settings.readPrefAdditionalSubtypes(prefs, context.resources)
            )
            .toList()

    private fun writeStyles(styles: List<InputMethodSubtype>) {
        val array = styles.toTypedArray()
        Settings.writePrefAdditionalSubtypes(
            prefs,
            AdditionalSubtypeUtils.createPrefSubtypes(array),
        )
        RichInputMethodManager.getInstance().setAdditionalInputMethodSubtypes(array)
        BackupManager(context).dataChanged()
    }

    // Additional subtypes are stored and matched by their legacy locale string, not a language tag.
    @Suppress("DEPRECATION")
    private fun localeOf(subtype: InputMethodSubtype): String = subtype.locale
}
