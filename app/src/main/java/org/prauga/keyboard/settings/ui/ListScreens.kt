/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListGroupScope
import com.prauga.pvot.designsystem.components.list.PvotListRow
import java.text.NumberFormat
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.SettingsSnapshot

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
