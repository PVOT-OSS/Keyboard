/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions

import java.util.function.Consumer
import org.prauga.keyboard.R
import org.prauga.keyboard.textfield.StripTextFieldClient

class EmojiSearchClient(
    private val onPick: Consumer<String>,
    private val onBackToPalette: Runnable,
) : StripTextFieldClient {
    override val hint = R.string.emoji_search_hint
    override val noResults = R.string.emoji_search_no_results
    override val largeResults = true

    override fun results(text: String) =
        EmojiKeywords.search(text).map { emoji ->
            StripTextFieldClient.Result(emoji, emoji) { onPick.accept(emoji) }
        }

    override fun onBack() = onBackToPalette.run()
}
