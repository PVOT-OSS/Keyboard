/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.list.PvotListGroupScope
import com.prauga.pvot.designsystem.components.list.PvotPreferenceRow
import com.prauga.pvot.designsystem.components.list.PvotSwitchRow
import org.prauga.keyboard.settings.data.SettingsSnapshot

internal fun PvotListGroupScope.prefSwitch(
    snapshot: SettingsSnapshot,
    key: String,
    default: Boolean,
    @StringRes title: Int,
    onEvent: (SettingsEvent) -> Unit,
    @StringRes summary: Int? = null,
    enabled: Boolean = true,
) =
    switchRow(
        title = title,
        checked = snapshot.boolean(key, default),
        onCheckedChange = { onEvent(SettingsEvent.SetBoolean(key, it)) },
        summary = summary,
        enabled = enabled,
    )

internal fun PvotListGroupScope.switchRow(
    @StringRes title: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @StringRes summary: Int? = null,
    enabled: Boolean = true,
) = item {
    PvotSwitchRow(
        title = stringResource(title),
        summary = summary?.let { stringResource(it) },
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
    )
}

internal fun PvotListGroupScope.linkRow(
    @StringRes title: Int,
    onClick: () -> Unit,
    summary: String? = null,
    @StringRes summaryRes: Int? = null,
    value: String? = null,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    showChevron: Boolean = true,
) = item {
    PvotPreferenceRow(
        title = stringResource(title),
        summary = summary ?: summaryRes?.let { stringResource(it) },
        value = value,
        icon = icon?.let { { Icon(painterResource(it), contentDescription = null) } },
        enabled = enabled,
        showChevron = showChevron,
        onClick = onClick,
    )
}
