/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup.ui

internal sealed interface SetupEvent {
    data object Launched : SetupEvent

    data object Refresh : SetupEvent

    data object Start : SetupEvent

    data object Back : SetupEvent

    data object EnableKeyboard : SetupEvent

    data object SwitchKeyboard : SetupEvent

    data object ConfigureLanguages : SetupEvent

    data object Finish : SetupEvent
}
