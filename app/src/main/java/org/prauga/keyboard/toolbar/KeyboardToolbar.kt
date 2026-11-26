/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.toolbar

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.prauga.keyboard.R

class KeyboardToolbar(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init {
        orientation = HORIZONTAL
    }

    fun createKey(
        @DrawableRes iconResId: Int,
        tint: ColorStateList,
        @StringRes contentDescriptionResId: Int,
        action: Runnable,
    ): ImageButton =
        ImageButton(context, null, R.attr.suggestionWordStyle).apply {
            setImageResource(iconResId)
            scaleType = ImageView.ScaleType.CENTER
            imageTintList = tint
            contentDescription = resources.getString(contentDescriptionResId)
            setOnClickListener { action.run() }
        }

    fun setKeys(keys: List<View>) {
        removeAllViews()
        for (key in keys) {
            addView(key, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
        }
    }
}
