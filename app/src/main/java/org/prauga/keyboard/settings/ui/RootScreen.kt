/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import android.content.Intent
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import org.prauga.keyboard.BuildConfig
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.Languages
import org.prauga.keyboard.settings.data.SettingsSnapshot

@Composable
internal fun RootScreen(
    snapshot: SettingsSnapshot,
    onOpen: (SettingsRoute) -> Unit,
    onBack: () -> Unit,
) {
    PvotSubScreen(screenTitle(SettingsRoute.ROOT), onBack) {
        snapshot.languages?.let { languages -> item { LanguagesGroup(languages) } }
        item { LinkGroup(MAIN_LINKS, onOpen) }
        item { LinkGroup(MORE_LINKS, onOpen) }
        item {
            PvotListGroup(title = stringResource(R.string.settings_group_about)) {
                item {
                    PvotListRow(
                        title = stringResource(R.string.app_version),
                        trailing = { Text(BuildConfig.VERSION_NAME) },
                    )
                }
                linkRow(R.string.open_source_licenses, { onOpen(SettingsRoute.LICENSES) })
            }
        }
    }
}

@Composable
private fun LanguagesGroup(languages: Languages) {
    val context = LocalContext.current
    val title = stringResource(R.string.select_language)
    PvotListGroup {
        linkRow(
            title = R.string.select_language,
            summary = languages.summary,
            icon = R.drawable.ic_settings_languages,
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS)
                        .putExtra(Settings.EXTRA_INPUT_METHOD_ID, languages.imeId)
                        .putExtra(Intent.EXTRA_TITLE, title)
                )
            },
        )
    }
}

@Composable
private fun LinkGroup(links: RootLinks, onOpen: (SettingsRoute) -> Unit) {
    PvotListGroup {
        links.items.forEach { linkRow(it.title, { onOpen(it.route) }, icon = it.icon) }
    }
}

@Immutable
private class RootLinks(vararg links: RootLink) {
    val items: List<RootLink> = links.toList()
}

private class RootLink(
    val route: SettingsRoute,
    @param:StringRes val title: Int,
    @param:DrawableRes val icon: Int,
)

private val MAIN_LINKS =
    RootLinks(
        RootLink(
            SettingsRoute.PREFERENCES,
            R.string.settings_screen_preferences,
            R.drawable.ic_settings_preferences,
        ),
        RootLink(
            SettingsRoute.APPEARANCE,
            R.string.settings_screen_appearance,
            R.drawable.ic_settings_appearance,
        ),
        RootLink(
            SettingsRoute.GESTURE,
            R.string.settings_screen_gesture,
            R.drawable.ic_settings_gesture,
        ),
        RootLink(
            SettingsRoute.CORRECTION,
            R.string.settings_screen_correction,
            R.drawable.ic_settings_spellcheck,
        ),
    )

private val MORE_LINKS =
    RootLinks(
        RootLink(SettingsRoute.TYPING_STATS, R.string.typing_stats, R.drawable.ic_settings_stats),
        RootLink(
            SettingsRoute.ADVANCED,
            R.string.settings_screen_advanced,
            R.drawable.ic_settings_advanced,
        ),
    )
