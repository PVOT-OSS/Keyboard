/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import androidx.annotation.StringRes
import org.prauga.keyboard.textfield.StripTextFieldClient
import java.util.function.Consumer

class ClipEditClient(
    override val initialText: String,
    @StringRes override val hint: Int,
    private val saveLabel: String,
    private val onSave: Consumer<String>,
    private val onBackToPanel: Runnable,
) : StripTextFieldClient {
    override val noResults = hint

    override fun results(text: String) =
        listOf(StripTextFieldClient.Result(saveLabel, saveLabel) { onSave.accept(text) })

    override fun onBack() = onBackToPanel.run()
}
