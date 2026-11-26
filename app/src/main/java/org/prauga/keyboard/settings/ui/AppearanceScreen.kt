/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import android.content.Intent
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListGroupScope
import org.prauga.keyboard.R
import org.prauga.keyboard.keyborders.BorderlessKeyBackground
import org.prauga.keyboard.keytextsize.KeyTextSize
import org.prauga.keyboard.photo.KeyboardPhoto
import org.prauga.keyboard.photo.KeyboardPhotoActivity
import org.prauga.keyboard.settings.Settings
import org.prauga.keyboard.settings.data.SettingsSnapshot

@Composable
internal fun AppearanceScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
    onOpen: (SettingsRoute) -> Unit,
    onBack: () -> Unit,
) {
    PvotSubScreen(screenTitle(SettingsRoute.APPEARANCE), onBack) {
        item { LookGroup(snapshot, onEvent, dialogs, onOpen) }
        item { PhotoGroup(snapshot, onEvent, dialogs) }
        item { LayoutGroup(snapshot, onEvent, onOpen) }
    }
}

@Composable
private fun LookGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
    onOpen: (SettingsRoute) -> Unit,
) {
    val themeNames = stringArrayResource(R.array.keyboard_theme_names).toList()
    val themeIds = integerArrayResource(R.array.keyboard_theme_ids).toList()
    val textSize =
        listSetting(
            KeyTextSize.PREF_KEY_TEXT_SIZE,
            R.string.key_text_size,
            R.array.key_text_size_entries,
            R.array.key_text_size_values,
            DEFAULT_TEXT_SIZE,
        )
    PvotListGroup(title = stringResource(R.string.settings_group_look)) {
        linkRow(
            title = R.string.settings_screen_theme,
            value = themeNames.getOrNull(themeIds.indexOf(snapshot.themeId)),
            onClick = { onOpen(SettingsRoute.THEME) },
        )
        prefSwitch(
            snapshot,
            BorderlessKeyBackground.PREF_KEY_BORDERS,
            true,
            R.string.key_borders,
            onEvent,
            R.string.key_borders_summary,
        )
        listRow(textSize, snapshot, dialogs, onEvent)
    }
}

@Composable
private fun PhotoGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
) {
    val context = LocalContext.current
    val darkness =
        listSetting(
            KeyboardPhoto.PREF_PHOTO_DARKNESS,
            R.string.keyboard_photo_darkness,
            R.array.keyboard_photo_darkness_entries,
            R.array.keyboard_photo_darkness_values,
            DEFAULT_DARKNESS,
        )
    PvotListGroup(title = stringResource(R.string.keyboard_photo)) {
        linkRow(
            title = R.string.keyboard_photo,
            summaryRes =
                if (snapshot.photoSet) R.string.keyboard_photo_set
                else R.string.keyboard_photo_summary,
            onClick = { context.startActivity(Intent(context, KeyboardPhotoActivity::class.java)) },
        )
        if (snapshot.photoSet) {
            listRow(darkness, snapshot, dialogs, onEvent)
            linkRow(
                title = R.string.keyboard_photo_remove,
                showChevron = false,
                onClick = { onEvent(SettingsEvent.RemovePhoto) },
            )
        }
    }
}

@Composable
private fun LayoutGroup(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onOpen: (SettingsRoute) -> Unit,
) {
    val split = snapshot.boolean(Settings.PREF_ENABLE_SPLIT_KEYBOARD, false)
    PvotListGroup(title = stringResource(R.string.settings_group_layout)) {
        linkRow(
            title = R.string.custom_input_styles_title,
            summary = snapshot.customStyles.joinToString(", ") { it.name }.ifEmpty { null },
            onClick = { onOpen(SettingsRoute.CUSTOM_STYLES) },
        )
        switches(
            snapshot,
            onEvent,
            listOf(
                SwitchSetting(
                    Settings.PREF_ENABLE_SPLIT_KEYBOARD,
                    false,
                    R.string.enable_split_keyboard,
                ),
                SwitchSetting(
                    Settings.PREF_SPLIT_KEYBOARD_DUPLICATE_KEYS,
                    true,
                    R.string.split_keyboard_duplicate_keys,
                    R.string.split_keyboard_duplicate_keys_summary,
                    enabled = split,
                ),
            ),
        )
    }
}

internal class ListSetting(
    val key: String,
    @param:StringRes val title: Int,
    val titleText: String,
    val entries: List<String>,
    val values: List<String>,
    val default: String,
)

@Composable
internal fun listSetting(
    key: String,
    @StringRes title: Int,
    @ArrayRes entries: Int,
    @ArrayRes values: Int,
    default: String,
): ListSetting =
    ListSetting(
        key,
        title,
        stringResource(title),
        stringArrayResource(entries).toList(),
        stringArrayResource(values).toList(),
        default,
    )

internal fun PvotListGroupScope.listRow(
    setting: ListSetting,
    snapshot: SettingsSnapshot,
    dialogs: SettingsDialogState,
    onEvent: (SettingsEvent) -> Unit,
    enabled: Boolean = true,
) {
    val index = setting.values.indexOf(snapshot.string(setting.key, setting.default))
    linkRow(
        title = setting.title,
        value = setting.entries.getOrNull(index),
        enabled = enabled,
        onClick = {
            dialogs.choice =
                ChoiceRequest(setting.titleText, setting.entries, index) {
                    onEvent(SettingsEvent.SetString(setting.key, setting.values[it]))
                }
        },
    )
}

private const val DEFAULT_TEXT_SIZE = "default"
private const val DEFAULT_DARKNESS = "medium"
