/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import android.Manifest
import android.app.backup.BackupManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
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
import org.prauga.keyboard.SystemBroadcastReceiver
import org.prauga.keyboard.blockedwords.BlockedWords
import org.prauga.keyboard.clipboard.ClipboardHistoryManager
import org.prauga.keyboard.compat.InputMethodSubtypeCompatUtils
import org.prauga.keyboard.keyboard.KeyboardTheme
import org.prauga.keyboard.personalization.PersonalizationHelper
import org.prauga.keyboard.photo.KeyboardPhoto
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.settings.SettingsValues
import org.prauga.keyboard.stats.TypingStats
import org.prauga.keyboard.toolbar.ToolbarKeys
import org.prauga.keyboard.utils.AdditionalSubtypeUtils
import org.prauga.keyboard.utils.SplitKeyboardUtils
import org.prauga.keyboard.utils.SubtypeLocaleUtils

internal class SettingsRepository(context: Context) {

    private val context: Context = context.applicationContext
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)

    val changes: Flow<Unit> =
        callbackFlow {
                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(Unit) }
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
                customStyles = customStyles(),
            )
        }

    suspend fun styleChoices(): StyleChoices =
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

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
        if (key == Settings.PREF_SHOW_SETUP_WIZARD_ICON) {
            SystemBroadcastReceiver.toggleAppIcon(context)
        }
        backup()
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        backup()
    }

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
        backup()
    }

    fun putPercent(key: String, percent: Int) {
        prefs.edit().putFloat(key, percent / PERCENT).apply()
        backup()
    }

    fun reset(key: String) {
        prefs.edit().remove(key).apply()
        backup()
    }

    fun setTheme(themeId: Int) {
        KeyboardTheme.saveKeyboardThemeId(themeId, prefs)
        backup()
    }

    fun setToolbarKeys(entries: List<ToolbarKeys.Entry>) {
        ToolbarKeys.write(prefs, entries)
        backup()
    }

    fun blockWord(word: String) {
        BlockedWords.add(prefs, word)
        backup()
    }

    fun unblockWord(word: String) {
        BlockedWords.remove(prefs, word)
        backup()
    }

    suspend fun removePhoto() = withContext(Dispatchers.IO) { KeyboardPhoto.remove(context) }

    fun clearClipboard() {
        ClipboardHistoryManager.getInstance(context).clearAll()
    }

    suspend fun clearLearnedWords() =
        withContext(Dispatchers.IO) { PersonalizationHelper.removeAllUserHistoryDictionaries(context) }

    suspend fun resetStats() = withContext(Dispatchers.IO) { TypingStats.reset(context) }

    suspend fun addStyle(locale: String, layout: String): AddStyleResult =
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

    suspend fun removeStyle(prefSubtype: String) =
        withContext(Dispatchers.IO) {
            RichInputMethodManager.init(context)
            writeStyles(
                currentStyles().filter { AdditionalSubtypeUtils.getPrefSubtype(it) != prefSubtype }
            )
        }

    fun playKeySound(percent: Int) {
        val audio = context.getSystemService(AudioManager::class.java) ?: return
        audio.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, percent / PERCENT)
    }

    fun vibrate(milliseconds: Int) {
        AudioAndHapticFeedbackManager.init(context)
        AudioAndHapticFeedbackManager.getInstance().vibrate(milliseconds.toLong())
    }

    private fun currentStyles(): List<InputMethodSubtype> =
        AdditionalSubtypeUtils.createAdditionalSubtypesArray(
                Settings.readPrefAdditionalSubtypes(prefs, context.resources)
            )
            .toList()

    private fun writeStyles(styles: List<InputMethodSubtype>) {
        val array = styles.toTypedArray()
        Settings.writePrefAdditionalSubtypes(prefs, AdditionalSubtypeUtils.createPrefSubtypes(array))
        RichInputMethodManager.getInstance().setAdditionalInputMethodSubtypes(array)
        backup()
    }

    private fun customStyles(): List<CustomStyle> =
        currentStyles().map {
            CustomStyle(
                AdditionalSubtypeUtils.getPrefSubtype(it),
                SubtypeLocaleUtils.getSubtypeDisplayNameInSystemLocale(it),
            )
        }

    private fun enabledLanguages(): Languages? {
        val imm = context.getSystemService(InputMethodManager::class.java) ?: return null
        val imi = imm.inputMethodList.firstOrNull { it.packageName == context.packageName } ?: return null
        val names =
            imm.getEnabledInputMethodSubtypeList(imi, true).map {
                it.getDisplayName(context, imi.packageName, imi.serviceInfo.applicationInfo)
                    .toString()
            }
        return Languages(imi.id, names.joinToString(", "))
    }

    // Additional subtypes are stored and matched by their legacy locale string, not a language tag.
    @Suppress("DEPRECATION")
    private fun localeOf(subtype: InputMethodSubtype): String = subtype.locale

    private fun backup() {
        BackupManager(context).dataChanged()
    }

    private fun percent(value: Float): Int = Math.round(value * PERCENT)

    private companion object {
        const val PERCENT = 100f
    }
}
