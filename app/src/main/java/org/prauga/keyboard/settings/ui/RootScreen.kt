/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.SettingsSnapshot

@Composable
internal fun RootScreen(
    snapshot: SettingsSnapshot,
    onOpen: (SettingsRoute) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val languagesTitle = stringResource(R.string.select_language)
    PvotSubScreen(screenTitle(SettingsRoute.ROOT), onBack) {
        val languages = snapshot.languages
        if (languages != null) {
            item {
                PvotListGroup {
                    linkRow(
                        title = R.string.select_language,
                        summary = languages.summary,
                        icon = R.drawable.ic_settings_languages,
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS)
                                    .putExtra(Settings.EXTRA_INPUT_METHOD_ID, languages.imeId)
                                    .putExtra(Intent.EXTRA_TITLE, languagesTitle)
                            )
                        },
                    )
                }
            }
        }
        item {
            PvotListGroup {
                linkRow(
                    R.string.settings_screen_preferences,
                    { onOpen(SettingsRoute.PREFERENCES) },
                    icon = R.drawable.ic_settings_preferences,
                )
                linkRow(
                    R.string.settings_screen_appearance,
                    { onOpen(SettingsRoute.APPEARANCE) },
                    icon = R.drawable.ic_settings_appearance,
                )
                linkRow(
                    R.string.settings_screen_gesture,
                    { onOpen(SettingsRoute.GESTURE) },
                    icon = R.drawable.ic_settings_gesture,
                )
                linkRow(
                    R.string.settings_screen_correction,
                    { onOpen(SettingsRoute.CORRECTION) },
                    icon = R.drawable.ic_settings_spellcheck,
                )
            }
        }
        item {
            PvotListGroup {
                linkRow(
                    R.string.typing_stats,
                    { onOpen(SettingsRoute.TYPING_STATS) },
                    icon = R.drawable.ic_settings_stats,
                )
                linkRow(
                    R.string.settings_screen_advanced,
                    { onOpen(SettingsRoute.ADVANCED) },
                    icon = R.drawable.ic_settings_advanced,
                )
            }
        }
    }
}
