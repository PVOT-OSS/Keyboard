/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.PvotSwitch
import com.prauga.pvot.designsystem.components.button.PvotIconButton
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListGroupScope
import com.prauga.pvot.designsystem.components.list.PvotListRow
import java.text.NumberFormat
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.toolbar.ToolbarKeys

@Composable
internal fun TypingStatsScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
    onBack: () -> Unit,
) {
    val stats = snapshot.stats
    val reset =
        ConfirmRequest(
            title = stringResource(R.string.typing_stats_reset),
            message = null,
            confirmLabel = stringResource(R.string.typing_stats_reset_confirm),
            destructive = true,
            onConfirm = { onEvent(SettingsEvent.ResetStats) },
        )
    PvotSubScreen(screenTitle(SettingsRoute.TYPING_STATS), onBack) {
        item {
            PvotListGroup {
                statRow(R.string.typing_stats_words, stats.words)
                statRow(R.string.typing_stats_glided, stats.glidedWords)
                statRow(R.string.typing_stats_autocorrections, stats.autocorrections)
                statRow(R.string.typing_stats_keystrokes_saved, stats.keystrokesSaved)
            }
        }
        item {
            PvotListGroup {
                linkRow(
                    title = R.string.typing_stats_reset,
                    summaryRes = R.string.typing_stats_privacy,
                    showChevron = false,
                    onClick = { dialogs.confirm = reset },
                )
            }
        }
    }
}

private fun PvotListGroupScope.statRow(@StringRes title: Int, value: Long) = item {
    PvotListRow(
        title = stringResource(title),
        trailing = {
            Text(
                NumberFormat.getIntegerInstance().format(value),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}

@Composable
internal fun ToolbarKeysScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
) {
    val entries = snapshot.toolbarKeys
    val update = { list: List<ToolbarKeys.Entry> -> onEvent(SettingsEvent.SetToolbarKeys(list)) }
    val upLabel = stringResource(R.string.toolbar_key_move_up)
    val downLabel = stringResource(R.string.toolbar_key_move_down)
    PvotSubScreen(screenTitle(SettingsRoute.TOOLBAR_KEYS), onBack) {
        item {
            PvotListGroup {
                entries.forEachIndexed { index, entry ->
                    item {
                        PvotListRow(
                            title = stringResource(toolbarKeyTitle(entry.id)),
                            onClick = {
                                update(
                                    entries.toMutableList().also {
                                        it[index] = entry.copy(shown = !entry.shown)
                                    }
                                )
                            },
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PvotIconButton(
                                        icon = painterResource(R.drawable.ic_editing_chevron_up),
                                        contentDescription = upLabel,
                                        enabled = index > 0,
                                        onClick = {
                                            update(
                                                entries.toMutableList().also {
                                                    it.add(index - 1, it.removeAt(index))
                                                }
                                            )
                                        },
                                    )
                                    PvotIconButton(
                                        icon = painterResource(R.drawable.ic_editing_chevron_down),
                                        contentDescription = downLabel,
                                        enabled = index < entries.size - 1,
                                        onClick = {
                                            update(
                                                entries.toMutableList().also {
                                                    it.add(index + 1, it.removeAt(index))
                                                }
                                            )
                                        },
                                    )
                                    PvotSwitch(
                                        checked = entry.shown,
                                        onCheckedChange = {
                                            update(
                                                entries.toMutableList().also { list ->
                                                    list[index] = entry.copy(shown = it)
                                                }
                                            )
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
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

@Composable
internal fun BlockedWordsScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
    onBack: () -> Unit,
) {
    val add =
        TextRequest(
            title = stringResource(R.string.blocked_words_add),
            hint = stringResource(R.string.blocked_words_input_hint),
            confirmLabel = stringResource(R.string.blocked_words_block),
            onConfirm = { onEvent(SettingsEvent.BlockWord(it)) },
        )
    val unblockLabel = stringResource(R.string.blocked_words_unblock)
    val unblockTitle = stringResource(R.string.blocked_words_unblock_title)
    PvotSubScreen(screenTitle(SettingsRoute.BLOCKED_WORDS), onBack) {
        item {
            PvotListGroup {
                linkRow(
                    title = R.string.blocked_words_add,
                    icon = R.drawable.ic_add_circle,
                    showChevron = false,
                    onClick = { dialogs.text = add },
                )
            }
        }
        item {
            PvotListGroup {
                if (snapshot.blockedWords.isEmpty()) {
                    item { PvotListRow(title = stringResource(R.string.blocked_words_empty)) }
                }
                snapshot.blockedWords.forEach { word ->
                    item {
                        PvotListRow(
                            title = word,
                            onClick = {
                                dialogs.confirm =
                                    ConfirmRequest(
                                        title = unblockTitle.format(word),
                                        message = null,
                                        confirmLabel = unblockLabel,
                                        destructive = false,
                                        onConfirm = { onEvent(SettingsEvent.UnblockWord(word)) },
                                    )
                            },
                        )
                    }
                }
            }
        }
    }
}
