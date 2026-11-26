/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext
import org.prauga.keyboard.AudioAndHapticFeedbackManager
import org.prauga.keyboard.R
import org.prauga.keyboard.RichInputMethodManager
import org.prauga.keyboard.blockedwords.BlockedWords
import org.prauga.keyboard.keyboard.KeyboardTheme
import org.prauga.keyboard.photo.KeyboardPhoto
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.settings.SettingsValues
import org.prauga.keyboard.stats.TypingStats
import org.prauga.keyboard.toolbar.ToolbarKeys
import org.prauga.keyboard.utils.SplitKeyboardUtils
import org.prauga.keyboard.utils.SubtypeLocaleUtils

internal class SettingsRepository(context: Context, private val styles: InputStylesRepository) {

    private val context: Context = context.applicationContext
    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this.context)

    val changes: Flow<Unit> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(Unit) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(Unit)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .conflate()

    suspend fun load(): SettingsSnapshot =
        withContext(Dispatchers.IO) {
            val res = context.resources
            AudioAndHapticFeedbackManager.init(context)
            RichInputMethodManager.init(context)
            SubtypeLocaleUtils.init(context)
            SettingsSnapshot(
                values = prefs.all.toMap(),
                languages = enabledLanguages(),
                hasVibrator = AudioAndHapticFeedbackManager.getInstance().hasVibrator(),
                showEmojiKeyOption = res.getBoolean(R.bool.config_enable_show_emoji_key_option),
                showVoiceKeyOption = res.getBoolean(R.bool.config_enable_show_voice_key_option),
                showPopupOption = Settings.readFromBuildConfigIfToShowKeyPreviewPopupOption(res),
                popupEnabled = Settings.readKeyPreviewPopupEnabled(prefs, res),
                vibrationEnabled = Settings.readVibrationEnabled(prefs, res),
                soundEnabled = Settings.readKeypressSoundEnabled(prefs, res),
                splitAvailable = SplitKeyboardUtils.isSplitKeyboardAvailable(res),
                themeId = KeyboardTheme.getSelectedKeyboardThemeId(context),
                photoSet = KeyboardPhoto.isSet(context),
                personalDictionaryAvailable =
                    context.packageManager.resolveActivity(
                        Intent(android.provider.Settings.ACTION_USER_DICTIONARY_SETTINGS),
                        PackageManager.MATCH_DEFAULT_ONLY,
                    ) != null,
                contactsGranted =
                    context.checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED,
                popupDismissDelay = Settings.readKeyPreviewPopupDismissDelay(prefs, res),
                popupDefaultDelay = res.getInteger(R.integer.config_key_preview_linger_timeout),
                vibrationMs = Settings.readKeypressVibrationDuration(prefs, res),
                soundPercent = percent(Settings.readKeypressSoundVolume(prefs, res)),
                soundDefaultPercent = percent(Settings.readDefaultKeypressSoundVolume(res)),
                longPressMs = Settings.readKeyLongpressTimeout(prefs, res),
                heightPercent =
                    percent(Settings.readKeyboardHeight(prefs, SettingsValues.DEFAULT_SIZE_SCALE)),
                stats = TypingStats.snapshot(context),
                toolbarKeys = ToolbarKeys.readEntries(prefs),
                blockedWords = BlockedWords.read(prefs).sorted(),
                customStyles = styles.customStyles(),
            )
        }

    private fun enabledLanguages(): Languages? {
        val imm = context.getSystemService(InputMethodManager::class.java)
        val imi =
            imm?.inputMethodList?.firstOrNull { it.packageName == context.packageName }
                ?: return null
        val names =
            imm.getEnabledInputMethodSubtypeList(imi, true).map {
                it.getDisplayName(context, imi.packageName, imi.serviceInfo.applicationInfo)
                    .toString()
            }
        return Languages(imi.id, names.joinToString(", "))
    }

    private fun percent(value: Float): Int = Math.round(value * PERCENT)
}
