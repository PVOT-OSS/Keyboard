/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.PvotSwitch
import com.prauga.pvot.designsystem.components.button.PvotIconButton
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.toolbar.ToolbarKeys

@Composable
internal fun ToolbarKeysScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
) {
    val entries = snapshot.toolbarKeys
    val update = { list: List<ToolbarKeys.Entry> -> onEvent(SettingsEvent.SetToolbarKeys(list)) }
    PvotSubScreen(screenTitle(SettingsRoute.TOOLBAR_KEYS), onBack) {
        item {
            PvotListGroup {
                entries.forEachIndexed { index, entry ->
                    item {
                        ToolbarKeyRow(
                            entry = entry,
                            canMoveUp = index > 0,
                            canMoveDown = index < entries.size - 1,
                            onMove = { offset ->
                                update(ToolbarKeys.moved(entries, index, index + offset))
                            },
                            onShownChange = { update(ToolbarKeys.withShown(entries, index, it)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarKeyRow(
    entry: ToolbarKeys.Entry,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (Int) -> Unit,
    onShownChange: (Boolean) -> Unit,
) {
    PvotListRow(
        title = stringResource(toolbarKeyTitle(entry.id)),
        onClick = { onShownChange(!entry.shown) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PvotIconButton(
                    icon = painterResource(R.drawable.ic_editing_chevron_up),
                    contentDescription = stringResource(R.string.toolbar_key_move_up),
                    enabled = canMoveUp,
                    onClick = { onMove(-1) },
                )
                PvotIconButton(
                    icon = painterResource(R.drawable.ic_editing_chevron_down),
                    contentDescription = stringResource(R.string.toolbar_key_move_down),
                    enabled = canMoveDown,
                    onClick = { onMove(1) },
                )
                PvotSwitch(checked = entry.shown, onCheckedChange = onShownChange)
            }
        },
    )
}

@StringRes
private fun toolbarKeyTitle(id: String): Int =
    when (id) {
        ToolbarKeys.CLIPBOARD -> R.string.clipboard_history
        ToolbarKeys.EDITING -> R.string.text_editing
        ToolbarKeys.ONE_HANDED -> R.string.one_handed_mode
        ToolbarKeys.RESIZE -> R.string.keyboard_resize
        ToolbarKeys.FLOAT -> R.string.spoken_description_float_keyboard
        ToolbarKeys.EMOJI -> R.string.spoken_description_emoji
        ToolbarKeys.INCOGNITO -> R.string.incognito_mode
        ToolbarKeys.MODES -> R.string.keyboard_modes
        else -> R.string.spoken_description_settings
    }
