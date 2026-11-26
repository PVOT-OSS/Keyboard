/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.components.PvotChoiceSheet
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotDialogActionStyle
import com.prauga.pvot.designsystem.components.slider.PvotSlider
import com.prauga.pvot.designsystem.components.textfield.PvotTextField
import com.prauga.pvot.designsystem.theme.PvotSpacing
import kotlin.math.roundToInt
import org.prauga.keyboard.R

internal class ChoiceRequest(
    val title: String,
    val entries: List<String>,
    val selected: Int,
    val onSelect: (Int) -> Unit,
)

internal class ConfirmRequest(
    val title: String,
    val message: String?,
    val confirmLabel: String,
    val destructive: Boolean,
    val onConfirm: () -> Unit,
)

internal class SliderRequest(
    val title: String,
    val value: Int,
    val range: IntRange,
    val step: Int,
    val format: (Int) -> String,
    val onPreview: (Int) -> Unit,
    val onSave: (Int) -> Unit,
    val onDefault: () -> Unit,
)

internal class DocumentRequest(val title: String, val text: String)

internal class TextRequest(
    val title: String,
    val hint: String,
    val confirmLabel: String,
    val onConfirm: (String) -> Unit,
)

@Stable
internal class SettingsDialogState {
    var choice by mutableStateOf<ChoiceRequest?>(null)
    var confirm by mutableStateOf<ConfirmRequest?>(null)
    var slider by mutableStateOf<SliderRequest?>(null)
    var text by mutableStateOf<TextRequest?>(null)
    var document by mutableStateOf<DocumentRequest?>(null)
}

@Composable
internal fun SettingsDialogs(state: SettingsDialogState) {
    val choiceShown = remember { mutableStateOf<ChoiceRequest?>(null) }
    state.choice?.let { choiceShown.value = it }
    choiceShown.value?.let { request ->
        PvotChoiceSheet(
            visible = state.choice != null,
            title = request.title,
            entries = request.entries,
            selected = request.selected,
            onDismiss = { state.choice = null },
            onSelect = {
                state.choice = null
                request.onSelect(it)
            },
        )
    }
    ConfirmDialog(state)
    SliderDialog(state)
    TextDialog(state)
    DocumentDialog(state)
}

@Composable
private fun DocumentDialog(state: SettingsDialogState) {
    val shown = remember { mutableStateOf<DocumentRequest?>(null) }
    state.document?.let { shown.value = it }
    val request = shown.value ?: return
    PvotDialog(
        visible = state.document != null,
        onDismissRequest = { state.document = null },
        title = request.title,
        content = {
            Text(
                text = request.text,
                style = MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier.fillMaxWidth()
                        .heightIn(max = DOCUMENT_MAX_HEIGHT)
                        .verticalScroll(rememberScrollState()),
            )
        },
        actions = {
            PvotDialogAction(
                label = stringResource(android.R.string.ok),
                onClick = { state.document = null },
                style = PvotDialogActionStyle.Primary,
            )
        },
    )
}

private val DOCUMENT_MAX_HEIGHT = 340.dp

@Composable
private fun ConfirmDialog(state: SettingsDialogState) {
    val shown = remember { mutableStateOf<ConfirmRequest?>(null) }
    state.confirm?.let { shown.value = it }
    val request = shown.value ?: return
    PvotDialog(
        visible = state.confirm != null,
        onDismissRequest = { state.confirm = null },
        title = request.title,
        message = request.message,
        destructive = request.destructive,
        actions = {
            PvotDialogAction(
                label = request.confirmLabel,
                onClick = {
                    request.onConfirm()
                    state.confirm = null
                },
                style =
                    if (request.destructive) PvotDialogActionStyle.Destructive
                    else PvotDialogActionStyle.Primary,
            )
            PvotDialogAction(
                label = stringResource(android.R.string.cancel),
                onClick = { state.confirm = null },
            )
        },
    )
}

@Composable
private fun SliderDialog(state: SettingsDialogState) {
    val shown = remember { mutableStateOf<SliderRequest?>(null) }
    state.slider?.let { shown.value = it }
    val request = shown.value ?: return
    var value by
        remember(request) {
            mutableFloatStateOf(request.value.coerceIn(request.range).toFloat())
        }
    val current = value.roundToInt()
    PvotDialog(
        visible = state.slider != null,
        onDismissRequest = { state.slider = null },
        title = request.title,
        content = {
            Text(
                text = request.format(current),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = PvotSpacing.S),
            )
            PvotSlider(
                value = value,
                onValueChange = { value = it },
                valueRange = request.range.first.toFloat()..request.range.last.toFloat(),
                steps = ((request.range.last - request.range.first) / request.step - 1).coerceAtLeast(0),
                onValueChangeFinished = { request.onPreview(value.roundToInt()) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        actions = {
            PvotDialogAction(
                label = stringResource(android.R.string.ok),
                onClick = {
                    request.onSave(current)
                    state.slider = null
                },
                style = PvotDialogActionStyle.Primary,
            )
            PvotDialogAction(
                label = stringResource(R.string.button_default),
                onClick = {
                    request.onDefault()
                    state.slider = null
                },
            )
            PvotDialogAction(
                label = stringResource(android.R.string.cancel),
                onClick = { state.slider = null },
            )
        },
    )
}

@Composable
private fun TextDialog(state: SettingsDialogState) {
    val shown = remember { mutableStateOf<TextRequest?>(null) }
    state.text?.let { shown.value = it }
    val request = shown.value ?: return
    var input by remember(request) { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(request) { focus.requestFocus() }
    PvotDialog(
        visible = state.text != null,
        onDismissRequest = { state.text = null },
        title = request.title,
        content = {
            PvotTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = request.hint,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        },
        actions = {
            PvotDialogAction(
                label = request.confirmLabel,
                enabled = input.isNotBlank(),
                onClick = {
                    request.onConfirm(input)
                    state.text = null
                },
                style = PvotDialogActionStyle.Primary,
            )
            PvotDialogAction(
                label = stringResource(android.R.string.cancel),
                onClick = { state.text = null },
            )
        },
    )
}
