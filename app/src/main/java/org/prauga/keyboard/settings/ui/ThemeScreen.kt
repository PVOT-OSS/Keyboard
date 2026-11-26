/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import android.content.Context
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.prauga.pvot.designsystem.components.PvotCard
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.theme.PvotShapes
import com.prauga.pvot.designsystem.theme.PvotSpacing
import com.prauga.pvot.designsystem.R as PvotR
import org.prauga.keyboard.R
import org.prauga.keyboard.RichInputMethodManager
import org.prauga.keyboard.keyboard.KeyboardId
import org.prauga.keyboard.keyboard.KeyboardLayoutSet
import org.prauga.keyboard.keyboard.KeyboardTheme
import org.prauga.keyboard.keyboard.KeyboardView
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.utils.SubtypeLocaleUtils

@Composable
internal fun ThemeScreen(
    snapshot: SettingsSnapshot,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
) {
    val names = stringArrayResource(R.array.keyboard_theme_names).toList()
    val ids = integerArrayResource(R.array.keyboard_theme_ids).toList()
    val selectedLabel = stringResource(R.string.keyboard_theme_selected)
    PvotSubScreen(screenTitle(SettingsRoute.THEME), onBack) {
        ids.forEachIndexed { index, id ->
            item(key = id) {
                ThemeCard(
                    name = names[index],
                    themeId = id,
                    selected = id == snapshot.themeId,
                    selectedLabel = selectedLabel,
                    onClick = { onEvent(SettingsEvent.SetTheme(id)) },
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    name: String,
    themeId: Int,
    selected: Boolean,
    selectedLabel: String,
    onClick: () -> Unit,
) {
    PvotCard(
        onClick = onClick,
        border = if (selected) BorderStroke(SELECTED_BORDER, MaterialTheme.colorScheme.primary) else null,
        modifier =
            Modifier.fillMaxWidth().semantics {
                role = Role.RadioButton
                this.selected = selected
            },
    ) {
        KeyboardPreview(
            themeId = themeId,
            modifier = Modifier.padding(PvotSpacing.S).clip(PvotShapes.Small),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(start = PvotSpacing.L, end = PvotSpacing.L, bottom = PvotSpacing.M),
        ) {
            Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (selected) {
                Icon(
                    painterResource(PvotR.drawable.pvot_ic_check),
                    contentDescription = selectedLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(CHECK_SIZE),
                )
            }
        }
    }
}

@Composable
private fun KeyboardPreview(themeId: Int, modifier: Modifier = Modifier) {
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
private val SELECTED_BORDER = 2.dp
private val CHECK_SIZE = 24.dp
