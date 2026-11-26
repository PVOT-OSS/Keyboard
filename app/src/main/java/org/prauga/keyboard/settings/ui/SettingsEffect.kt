/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.annotation.StringRes

internal sealed interface SettingsEffect {
    data class Message(@param:StringRes val text: Int, val arg: String? = null) : SettingsEffect

    data class OfferEnableStyle(val imeId: String) : SettingsEffect

    data class ShowDocument(val title: String, val text: String) : SettingsEffect
}
