/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import org.prauga.keyboard.R

@Composable
internal fun LicensesScreen(onEvent: (SettingsEvent) -> Unit, onBack: () -> Unit) {
    val names = stringArrayResource(R.array.license_names)
    val notices = stringArrayResource(R.array.license_notices)
    val files = stringArrayResource(R.array.license_files)
    PvotSubScreen(screenTitle(SettingsRoute.LICENSES), onBack) {
        item {
            PvotListGroup {
                names.forEachIndexed { index, name ->
                    item {
                        PvotListRow(
                            title = name,
                            supportingText = notices[index],
                            onClick = { onEvent(SettingsEvent.ShowLicense(name, files[index])) },
                        )
                    }
                }
            }
        }
    }
}
