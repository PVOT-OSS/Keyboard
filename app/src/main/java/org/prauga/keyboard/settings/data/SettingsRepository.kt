/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

import kotlinx.coroutines.flow.Flow

internal interface SettingsRepository {
    val changes: Flow<Unit>

    suspend fun load(): SettingsSnapshot
}
