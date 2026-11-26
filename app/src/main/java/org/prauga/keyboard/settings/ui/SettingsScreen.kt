/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prauga.pvot.coreui.UiState
import org.prauga.keyboard.R
import org.prauga.keyboard.settings.data.DeviceSettingsActions
import org.prauga.keyboard.settings.data.PreferenceSettingsRepository
import org.prauga.keyboard.settings.data.PreferenceSettingsWriter
import org.prauga.keyboard.settings.data.SettingsSnapshot
import org.prauga.keyboard.settings.data.StyleChoices
import org.prauga.keyboard.settings.data.SystemInputStylesRepository
import org.prauga.keyboard.utils.IntentUtils

internal enum class SettingsRoute {
    ROOT,
    PREFERENCES,
    APPEARANCE,
    THEME,
    GESTURE,
    CORRECTION,
    ADVANCED,
    TYPING_STATS,
    TOOLBAR_KEYS,
    BLOCKED_WORDS,
    CUSTOM_STYLES,
    LICENSES,
}

internal class SettingsNavigator(initial: List<SettingsRoute>) {
    var stack by mutableStateOf(initial)
        private set

    val current: SettingsRoute
        get() = stack.last()

    fun open(route: SettingsRoute) {
        stack = stack + route
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack = stack.dropLast(1)
        return true
    }
}

@Composable
internal fun SettingsScreen(
    onExit: () -> Unit,
    viewModel: SettingsViewModel = settingsViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val choices by viewModel.styleChoices.collectAsStateWithLifecycle()
    val navigator =
        rememberSaveable(
            saver =
                listSaver(
                    save = { nav -> nav.stack.map { it.name } },
                    restore = { names ->
                        SettingsNavigator(names.map { SettingsRoute.valueOf(it) })
                    },
                )
        ) {
            SettingsNavigator(listOf(SettingsRoute.ROOT))
        }
    val dialogs = remember { SettingsDialogState() }
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(SettingsEvent.Refresh)
        onPauseOrDispose {}
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect -> handleEffect(context, effect, dialogs) }
    }
    BackHandler(enabled = navigator.stack.size > 1) { navigator.back() }

    val snapshot = (state as? UiState.Success)?.data ?: return
    val holder = rememberSaveableStateHolder()
    val back: () -> Unit = { if (!navigator.back()) onExit() }
    AnimatedContent(
        targetState = navigator.current,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "settings",
    ) { route ->
        holder.SaveableStateProvider(route.name) {
            SettingsRouteContent(
                route,
                snapshot,
                choices,
                viewModel::onEvent,
                viewModel::loadStyleChoices,
                navigator,
                dialogs,
                back,
            )
        }
    }
    SettingsDialogs(dialogs)
}

@Composable
private fun SettingsRouteContent(
    route: SettingsRoute,
    snapshot: SettingsSnapshot,
    choices: StyleChoices?,
    onEvent: (SettingsEvent) -> Unit,
    onLoadStyles: () -> Unit,
    navigator: SettingsNavigator,
    dialogs: SettingsDialogState,
    onBack: () -> Unit,
) {
    when (route) {
        SettingsRoute.ROOT -> RootScreen(snapshot, navigator::open, onBack)
        SettingsRoute.PREFERENCES -> PreferencesScreen(snapshot, onEvent, navigator::open, onBack)
        SettingsRoute.APPEARANCE ->
            AppearanceScreen(snapshot, onEvent, dialogs, navigator::open, onBack)
        SettingsRoute.THEME -> ThemeScreen(snapshot, onEvent, onBack)
        SettingsRoute.GESTURE -> GestureScreen(snapshot, onEvent, onBack)
        SettingsRoute.CORRECTION ->
            CorrectionScreen(snapshot, onEvent, dialogs, navigator::open, onBack)
        SettingsRoute.ADVANCED -> AdvancedScreen(snapshot, onEvent, dialogs, onBack)
        SettingsRoute.TYPING_STATS -> TypingStatsScreen(snapshot, onEvent, dialogs, onBack)
        SettingsRoute.TOOLBAR_KEYS -> ToolbarKeysScreen(snapshot, onEvent, onBack)
        SettingsRoute.BLOCKED_WORDS -> BlockedWordsScreen(snapshot, onEvent, dialogs, onBack)
        SettingsRoute.LICENSES -> LicensesScreen(onEvent, onBack)
        SettingsRoute.CUSTOM_STYLES -> {
            LaunchedEffect(Unit) { onLoadStyles() }
            CustomStylesScreen(snapshot, choices, onEvent, dialogs, onBack)
        }
    }
}

@Composable
internal fun settingsViewModel(): SettingsViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel {
        val styles = SystemInputStylesRepository(context)
        SettingsViewModel(
            PreferenceSettingsRepository(context, styles),
            PreferenceSettingsWriter(context),
            styles,
            DeviceSettingsActions(context),
        )
    }
}

@Composable
internal fun screenTitle(route: SettingsRoute): String =
    when (route) {
        SettingsRoute.ROOT -> stringResource(R.string.english_ime_settings)
        SettingsRoute.PREFERENCES -> stringResource(R.string.settings_screen_preferences)
        SettingsRoute.APPEARANCE -> stringResource(R.string.settings_screen_appearance)
        SettingsRoute.THEME -> stringResource(R.string.settings_screen_theme)
        SettingsRoute.GESTURE -> stringResource(R.string.settings_screen_gesture)
        SettingsRoute.CORRECTION -> stringResource(R.string.settings_screen_correction)
        SettingsRoute.ADVANCED -> stringResource(R.string.settings_screen_advanced)
        SettingsRoute.TYPING_STATS -> stringResource(R.string.typing_stats)
        SettingsRoute.TOOLBAR_KEYS -> stringResource(R.string.toolbar_keys)
        SettingsRoute.BLOCKED_WORDS -> stringResource(R.string.blocked_words)
        SettingsRoute.CUSTOM_STYLES -> stringResource(R.string.custom_input_styles_title)
        SettingsRoute.LICENSES -> stringResource(R.string.open_source_licenses)
    }

private fun handleEffect(context: Context, effect: SettingsEffect, dialogs: SettingsDialogState) {
    when (effect) {
        is SettingsEffect.Message -> {
            val text =
                if (effect.arg == null) context.getString(effect.text)
                else context.getString(effect.text, effect.arg)
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
        is SettingsEffect.ShowDocument ->
            dialogs.document = DocumentRequest(effect.title, effect.text)
        is SettingsEffect.OfferEnableStyle ->
            dialogs.confirm =
                ConfirmRequest(
                    title = context.getString(R.string.custom_input_styles_title),
                    message = context.getString(R.string.custom_input_style_note_message),
                    confirmLabel = context.getString(R.string.enable),
                    destructive = false,
                    onConfirm = {
                        context.startActivity(
                            IntentUtils.getInputLanguageSelectionIntent(
                                effect.imeId,
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP,
                            )
                        )
                    },
                )
    }
}
