/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.data

internal interface InputStylesRepository {
    suspend fun styleChoices(): StyleChoices

    suspend fun addStyle(locale: String, layout: String): AddStyleResult

    suspend fun removeStyle(prefSubtype: String)

    fun customStyles(): List<CustomStyle>
}
