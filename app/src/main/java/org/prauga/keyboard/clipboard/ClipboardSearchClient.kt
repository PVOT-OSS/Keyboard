/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import org.prauga.keyboard.R
import org.prauga.keyboard.textfield.StripTextFieldClient
import java.util.function.Consumer

class ClipboardSearchClient(
    private val manager: ClipboardHistoryManager,
    private val onPaste: Consumer<String>,
    private val onBackToPanel: Runnable,
) : StripTextFieldClient {
    override val hint = R.string.clipboard_history_search
    override val noResults = R.string.clipboard_history_no_results

    override fun results(text: String) =
        manager.getItems()
            .filter { it.text.contains(text.trim(), ignoreCase = true) }
            .map { item ->
                StripTextFieldClient.Result(label(item.text), item.text) { onPaste.accept(item.text) }
            }

    override fun onBack() = onBackToPanel.run()

    private fun label(text: String): String {
        val line = text.trim().lineSequence().first()
        return if (line.length > MAX_LABEL) line.take(MAX_LABEL) + "…" else line
    }

    private companion object {
        const val MAX_LABEL = 30
    }
}
