/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import org.prauga.keyboard.R
import org.prauga.keyboard.autospace.AutoSpace
import org.prauga.keyboard.multilingual.MultilingualLocales
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.userdictionary.PersonalDictionaryBackupActivity

@Composable
internal fun CorrectionScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
    onOpen: (SettingsRoute) -> Unit,
    onBack: () -> Unit,
) {
    PvotSubScreen(screenTitle(SettingsRoute.CORRECTION), onBack) {
        item { DictionariesGroup(snapshot, onEvent, onOpen) }
        item { CorrectionsGroup(snapshot, onEvent, dialogs) }
        item { SuggestionsGroup(snapshot, onEvent, dialogs) }
        item { ClipboardGroup(snapshot, onEvent, dialogs) }
    }
}

@Composable
private fun DictionariesGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onOpen: (SettingsRoute) -> Unit,
) {
    val context = LocalContext.current
    val offensiveDefault = booleanResource(R.bool.config_block_potentially_offensive)
    val backup = { action: String ->
        context.startActivity(
            Intent(context, PersonalDictionaryBackupActivity::class.java).setAction(action)
        )
    }
    PvotListGroup(title = stringResource(R.string.settings_group_dictionaries)) {
        if (snapshot.personalDictionaryAvailable) {
            linkRow(
                title = R.string.edit_personal_dictionary,
                onClick = {
                    context.startActivity(
                        Intent(android.provider.Settings.ACTION_USER_DICTIONARY_SETTINGS)
                    )
                },
            )
        }
        linkRow(
            title = R.string.export_personal_dictionary,
            summaryRes = R.string.export_personal_dictionary_summary,
            onClick = { backup(PersonalDictionaryBackupActivity.ACTION_EXPORT) },
        )
        linkRow(
            title = R.string.import_personal_dictionary,
            summaryRes = R.string.import_personal_dictionary_summary,
            onClick = { backup(PersonalDictionaryBackupActivity.ACTION_IMPORT) },
        )
        linkRow(
            title = R.string.blocked_words,
            summaryRes = R.string.blocked_words_summary,
            onClick = { onOpen(SettingsRoute.BLOCKED_WORDS) },
        )
        prefSwitch(
            snapshot,
            Settings.PREF_BLOCK_POTENTIALLY_OFFENSIVE,
            offensiveDefault,
            R.string.prefs_block_potentially_offensive_title,
            onEvent,
            R.string.prefs_block_potentially_offensive_summary,
        )
    }
}

@Composable
private fun CorrectionsGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
) {
    val autoCorrection = snapshot.boolean(Settings.PREF_AUTO_CORRECTION, true)
    val strength =
        listSetting(
            Settings.PREF_AUTO_CORRECTION_STRENGTH,
            R.string.auto_correction_strength,
            R.array.auto_correction_strength_entries,
            R.array.auto_correction_strength_values,
            stringResource(R.string.auto_correction_threshold_mode_index_modest),
        )
    PvotListGroup(title = stringResource(R.string.settings_group_corrections)) {
        prefSwitch(
            snapshot,
            Settings.PREF_AUTO_CORRECTION,
            true,
            R.string.auto_correction,
            onEvent,
            R.string.auto_correction_summary,
        )
        listRow(strength, snapshot, dialogs, onEvent, enabled = autoCorrection)
        switches(
            snapshot,
            onEvent,
            listOf(
                SwitchSetting(
                    Settings.PREF_UNDO_AUTO_CORRECTION_ON_BACKSPACE,
                    true,
                    R.string.undo_auto_correction_on_backspace,
                    R.string.undo_auto_correction_on_backspace_summary,
                    enabled = autoCorrection,
                ),
                SwitchSetting(
                    AutoSpace.PREF_AUTO_SPACE_AFTER_PUNCTUATION,
                    false,
                    R.string.auto_space_after_punctuation,
                    R.string.auto_space_after_punctuation_summary,
                ),
            ),
        )
    }
}

@Composable
private fun SuggestionsGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
) {
    val prediction = snapshot.boolean(Settings.PREF_BIGRAM_PREDICTIONS, true)
    val contactsPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted)
                onEvent(SettingsEvent.SetBoolean(Settings.PREF_KEY_USE_CONTACTS_DICT, true))
        }
    val clearLearned =
        ConfirmRequest(
            title = stringResource(R.string.clear_learned_words_confirm_title),
            message = stringResource(R.string.clear_learned_words_confirm_message),
            confirmLabel = stringResource(R.string.clear_learned_words_confirm),
            destructive = true,
            onConfirm = { onEvent(SettingsEvent.ClearLearnedWords) },
        )
    PvotListGroup(title = stringResource(R.string.settings_group_suggestions)) {
        switches(snapshot, onEvent, suggestionSwitches(prediction))
        switchRow(
            title = R.string.use_contacts_dict,
            summary = R.string.use_contacts_dict_summary,
            checked =
                snapshot.contactsGranted &&
                    snapshot.boolean(Settings.PREF_KEY_USE_CONTACTS_DICT, true),
            onCheckedChange = { on ->
                if (on && !snapshot.contactsGranted) {
                    contactsPermission.launch(Manifest.permission.READ_CONTACTS)
                } else {
                    onEvent(SettingsEvent.SetBoolean(Settings.PREF_KEY_USE_CONTACTS_DICT, on))
                }
            },
        )
        linkRow(
            title = R.string.clear_learned_words,
            summaryRes = R.string.clear_learned_words_summary,
            showChevron = false,
            onClick = { dialogs.confirm = clearLearned },
        )
    }
}

private fun suggestionSwitches(prediction: Boolean) =
    listOf(
        SwitchSetting(
            Settings.PREF_SHOW_SUGGESTIONS,
            true,
            R.string.prefs_show_suggestions,
            R.string.prefs_show_suggestions_summary,
        ),
        SwitchSetting(
            Settings.PREF_BIGRAM_PREDICTIONS,
            true,
            R.string.bigram_prediction,
            R.string.bigram_prediction_summary,
        ),
        SwitchSetting(
            Settings.PREF_SPACE_INSERTS_PREDICTION,
            false,
            R.string.space_inserts_prediction,
            R.string.space_inserts_prediction_summary,
            enabled = prediction,
        ),
        SwitchSetting(
            Settings.PREF_EMOJI_SUGGESTIONS,
            true,
            R.string.emoji_suggestions,
            R.string.emoji_suggestions_summary,
        ),
        SwitchSetting(
            MultilingualLocales.PREF_MULTILINGUAL_TYPING,
            true,
            R.string.multilingual_typing,
            R.string.multilingual_typing_summary,
        ),
        SwitchSetting(
            Settings.PREF_KEY_USE_PERSONALIZED_DICTS,
            true,
            R.string.use_personalized_dicts,
            R.string.use_personalized_dicts_summary,
        ),
    )

@Composable
private fun ClipboardGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
) {
    val clearClipboard =
        ConfirmRequest(
            title = stringResource(R.string.clear_clipboard_history_confirm_title),
            message = stringResource(R.string.clear_clipboard_history_confirm_message),
            confirmLabel = stringResource(R.string.clear_clipboard_history_confirm),
            destructive = true,
            onConfirm = { onEvent(SettingsEvent.ClearClipboard) },
        )
    PvotListGroup(title = stringResource(R.string.settings_group_clipboard)) {
        switches(
            snapshot,
            onEvent,
            listOf(
                SwitchSetting(
                    Settings.PREF_CLIPBOARD_HISTORY,
                    true,
                    R.string.clipboard_history,
                    R.string.clipboard_history_summary,
                ),
                SwitchSetting(
                    Settings.PREF_SHOW_CLIPBOARD_SUGGESTIONS,
                    true,
                    R.string.show_clipboard_suggestions,
                    R.string.show_clipboard_suggestions_summary,
                ),
            ),
        )
        linkRow(
            title = R.string.clear_clipboard_history,
            summaryRes = R.string.clear_clipboard_history_summary,
            showChevron = false,
            onClick = { dialogs.confirm = clearClipboard },
        )
    }
}
