/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.prauga.keyboard.SystemBroadcastReceiver
import org.prauga.keyboard.blockedwords.BlockedWords
import org.prauga.keyboard.keyboard.KeyboardTheme
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.toolbar.ToolbarKeys

internal class PreferenceSettingsWriter(context: Context) : SettingsWriter {

    private val context: Context = context.applicationContext
    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this.context)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
        if (key == Settings.PREF_SHOW_SETUP_WIZARD_ICON) {
            SystemBroadcastReceiver.toggleAppIcon(context)
        }
        backup()
    }

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        backup()
    }

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
        backup()
    }

    override fun putPercent(key: String, percent: Int) {
        prefs.edit().putFloat(key, percent / PERCENT).apply()
        backup()
    }

    override fun reset(key: String) {
        prefs.edit().remove(key).apply()
        backup()
    }

    override fun setTheme(themeId: Int) {
        KeyboardTheme.saveKeyboardThemeId(themeId, prefs)
        backup()
    }

    override fun setToolbarKeys(entries: List<ToolbarKeys.Entry>) {
        ToolbarKeys.write(prefs, entries)
        backup()
    }

    override fun blockWord(word: String) {
        BlockedWords.add(prefs, word)
        backup()
    }

    override fun unblockWord(word: String) {
        BlockedWords.remove(prefs, word)
        backup()
    }

    private fun backup() {
        BackupManager(context).dataChanged()
    }
}
