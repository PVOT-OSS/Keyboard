/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.preview

import android.content.Context
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import org.prauga.keyboard.RichInputMethodManager
import org.prauga.keyboard.keyboard.KeyboardId
import org.prauga.keyboard.keyboard.KeyboardLayoutSet
import org.prauga.keyboard.keyboard.KeyboardTheme
import org.prauga.keyboard.keyboard.KeyboardView
import org.prauga.keyboard.utils.SubtypeLocaleUtils

@Composable
internal fun KeyboardPreview(themeId: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val width = constraints.maxWidth
        val height = (width * PREVIEW_ASPECT).toInt()
        val heightDp = with(LocalDensity.current) { height.toDp() }
        AndroidView(
            factory = { context -> previewView(context, themeId, width, height) },
            modifier = Modifier.fillMaxWidth().height(heightDp),
        )
    }
}

private fun previewView(context: Context, themeId: Int, width: Int, height: Int): View {
    val themed = ContextThemeWrapper(context, KeyboardTheme.getStyleId(context, themeId))
    RichInputMethodManager.init(context)
    SubtypeLocaleUtils.init(context)
    // Cached keyboards are keyed by layout and size but not by theme.
    KeyboardLayoutSet.onKeyboardThemeChanged()
    val editorInfo = EditorInfo().apply { inputType = InputType.TYPE_CLASS_TEXT }
    val keyboard =
        KeyboardLayoutSet.Builder(themed, editorInfo)
            .setKeyboardGeometry(width, height)
            .setSubtype(RichInputMethodManager.getInstance().currentSubtype)
            .build()
            .getKeyboard(KeyboardId.ELEMENT_ALPHABET)
    return KeyboardView(themed, null).apply {
        setKeyboard(keyboard)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }
}

private const val PREVIEW_ASPECT = 0.62f
