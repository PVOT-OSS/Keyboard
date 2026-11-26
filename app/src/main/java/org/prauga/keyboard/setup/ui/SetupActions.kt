/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup.ui

internal class SetupActions(
    val onStart: () -> Unit,
    val onEnable: () -> Unit,
    val onSwitch: () -> Unit,
    val onLanguages: () -> Unit,
    val onFinish: () -> Unit,
)
