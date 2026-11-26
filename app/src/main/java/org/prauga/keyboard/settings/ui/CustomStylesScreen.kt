/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.settings.data.StyleChoices

@Composable
internal fun CustomStylesScreen(
    snapshot: SettingsSnapshot,
    choices: StyleChoices?,
    onEvent: (SettingsEvent) -> Unit,
    dialogs: SettingsDialogState,
    onBack: () -> Unit,
) {
    val languageTitle = stringResource(R.string.subtype_locale)
    val layoutTitle = stringResource(R.string.keyboard_layout_set)
    val removeTitle = stringResource(R.string.custom_input_style_remove_title)
    val removeLabel = stringResource(R.string.remove)
    val pickLayout = { locale: String ->
        val layouts = choices?.layouts.orEmpty()
        dialogs.choice =
            ChoiceRequest(layoutTitle, layouts.map { it.label }, -1) {
                onEvent(SettingsEvent.AddStyle(locale, layouts[it].value))
            }
    }
    PvotSubScreen(screenTitle(SettingsRoute.CUSTOM_STYLES), onBack) {
        item {
            PvotListGroup {
                linkRow(
                    title = R.string.add_style,
                    icon = R.drawable.ic_add_circle,
                    showChevron = false,
                    enabled = choices != null,
                    onClick = {
                        val locales = choices?.locales.orEmpty()
                        dialogs.choice =
                            ChoiceRequest(languageTitle, locales.map { it.label }, -1) {
                                pickLayout(locales[it].value)
                            }
                    },
                )
            }
        }
        item {
            PvotListGroup {
                if (snapshot.customStyles.isEmpty()) {
                    item { PvotListRow(title = stringResource(R.string.custom_input_styles_empty)) }
                }
                snapshot.customStyles.forEach { style ->
                    item {
                        PvotListRow(
                            title = style.name,
                            onClick = {
                                dialogs.confirm =
                                    ConfirmRequest(
                                        title = removeTitle.format(style.name),
                                        message = null,
                                        confirmLabel = removeLabel,
                                        destructive = true,
                                        onConfirm = {
                                            onEvent(SettingsEvent.RemoveStyle(style.prefSubtype))
                                        },
                                    )
                            },
                        )
                    }
                }
            }
        }
    }
}
