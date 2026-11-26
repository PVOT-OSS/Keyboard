/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.setup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.R as PvotR
import com.prauga.pvot.designsystem.components.PvotCard
import com.prauga.pvot.designsystem.components.PvotSubScreen
import com.prauga.pvot.designsystem.components.button.PvotButton
import com.prauga.pvot.designsystem.components.button.PvotButtonStyle
import com.prauga.pvot.designsystem.theme.PvotShapes
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.keyboard.R
import org.prauga.keyboard.keyboard.KeyboardTheme
import org.prauga.keyboard.preview.KeyboardPreview
import org.prauga.keyboard.setup.SetupStep

internal class SetupActions(
    val onStart: () -> Unit,
    val onEnable: () -> Unit,
    val onSwitch: () -> Unit,
    val onLanguages: () -> Unit,
    val onFinish: () -> Unit,
)

@Composable
internal fun SetupWizardScreen(state: SetupUiState, appName: String, actions: SetupActions) {
    val step = state.step ?: return
    if (state.welcome) {
        WelcomeScreen(appName, actions.onStart)
    } else {
        StepsScreen(step, appName, actions)
    }
}

@Composable
private fun WelcomeScreen(appName: String, onStart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(PvotSpacing.L),
        ) {
            Text(
                stringResource(R.string.setup_welcome_title, appName),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.setup_welcome_additional_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = PvotSpacing.S),
            )
            KeyboardPreview(
                themeId = KeyboardTheme.THEME_ID_PVOT,
                modifier =
                    Modifier.padding(vertical = PvotSpacing.L * 2).clip(PvotShapes.Card),
            )
            PvotButton(
                text = stringResource(R.string.setup_start_action),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StepsScreen(step: SetupStep, appName: String, actions: SetupActions) {
    PvotSubScreen(stringResource(R.string.setup_steps_title, appName), actions.onFinish) {
        item {
            StepCard(
                number = 1,
                done = step > SetupStep.ENABLE,
                current = step == SetupStep.ENABLE,
                title = stringResource(R.string.setup_step1_title, appName),
                text =
                    if (step > SetupStep.ENABLE) {
                        stringResource(R.string.setup_step1_finished_instruction, appName)
                    } else {
                        stringResource(R.string.setup_step1_instruction, appName)
                    },
            ) {
                PvotButton(
                    text = stringResource(R.string.setup_step1_action),
                    onClick = actions.onEnable,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            StepCard(
                number = 2,
                done = step > SetupStep.SWITCH,
                current = step == SetupStep.SWITCH,
                title = stringResource(R.string.setup_step2_title, appName),
                text = stringResource(R.string.setup_step2_instruction, appName),
            ) {
                PvotButton(
                    text = stringResource(R.string.setup_step2_action),
                    onClick = actions.onSwitch,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            StepCard(
                number = 3,
                done = false,
                current = step == SetupStep.DONE,
                title = stringResource(R.string.setup_step3_title),
                text = stringResource(R.string.setup_step3_instruction, appName),
            ) {
                PvotButton(
                    text = stringResource(R.string.setup_step3_action),
                    onClick = actions.onLanguages,
                    style = PvotButtonStyle.Tonal,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(PvotSpacing.S))
                PvotButton(
                    text = stringResource(R.string.setup_finish_action),
                    onClick = actions.onFinish,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    done: Boolean,
    current: Boolean,
    title: String,
    text: String,
    action: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    PvotCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(PvotSpacing.L)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier.size(BADGE_SIZE)
                            .clip(CircleShape)
                            .background(if (current || done) colors.primary else colors.surfaceVariant),
                ) {
                    if (done) {
                        Icon(
                            painterResource(PvotR.drawable.pvot_ic_check),
                            contentDescription = null,
                            tint = colors.onPrimary,
                            modifier = Modifier.size(CHECK_SIZE),
                        )
                    } else {
                        Text(
                            number.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (current) colors.onPrimary else colors.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (current || done) colors.onSurface else colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = PvotSpacing.M),
                )
            }
            if (current || done) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = PvotSpacing.S),
                )
            }
            if (current) {
                Spacer(Modifier.height(PvotSpacing.L))
                action()
            }
        }
    }
}

private val BADGE_SIZE = 32.dp
private val CHECK_SIZE = 18.dp
