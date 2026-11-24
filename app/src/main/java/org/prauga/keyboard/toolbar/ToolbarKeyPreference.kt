/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.toolbar

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import org.prauga.keyboard.R

class ToolbarKeyPreference(context: Context) : Preference(context) {
    var shown = true
    var canMoveUp = false
    var canMoveDown = false
    var onToggle: () -> Unit = {}
    var onMoveUp: () -> Unit = {}
    var onMoveDown: () -> Unit = {}

    init {
        isPersistent = false
        isIconSpaceReserved = false
        widgetLayoutResource = R.layout.toolbar_key_preference_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.findViewById(R.id.toolbar_key_up).apply {
            isEnabled = canMoveUp
            alpha = if (canMoveUp) 1f else DISABLED_ALPHA
            setOnClickListener { onMoveUp() }
        }
        holder.findViewById(R.id.toolbar_key_down).apply {
            isEnabled = canMoveDown
            alpha = if (canMoveDown) 1f else DISABLED_ALPHA
            setOnClickListener { onMoveDown() }
        }
        (holder.findViewById(R.id.toolbar_key_switch) as CompoundButton).isChecked = shown
    }

    override fun onClick() {
        onToggle()
    }

    private companion object {
        const val DISABLED_ALPHA = 0.3f
    }
}
