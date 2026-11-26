/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup.ui

internal sealed interface SetupEffect {
    data object OpenImeSettings : SetupEffect

    data object ShowImePicker : SetupEffect

    data class OpenLanguages(val imeId: String) : SetupEffect

    data object OpenSettings : SetupEffect

    data object ReturnToWizard : SetupEffect

    data object Close : SetupEffect
}
