/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.settings

import android.os.Bundle
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme
import org.prauga.keyboard.settings.ui.SettingsScreen
import org.prauga.keyboard.utils.StatsUtils

class SettingsActivity : PvotBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatsUtils.onSettingsActivity(
            intent.getStringExtra(EXTRA_ENTRY_KEY) ?: EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS
        )
        setPvotContent { PvotAppTheme { SettingsScreen(onExit = ::finish) } }
    }

    companion object {
        const val EXTRA_SHOW_HOME_AS_UP = "show_home_as_up"
        const val EXTRA_ENTRY_KEY = "entry"
        const val EXTRA_ENTRY_VALUE_LONG_PRESS_COMMA = "long_press_comma"
        const val EXTRA_ENTRY_VALUE_APP_ICON = "app_icon"
        const val EXTRA_ENTRY_VALUE_NOTICE_DIALOG = "important_notice"
        const val EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS = "system_settings"
    }
}
