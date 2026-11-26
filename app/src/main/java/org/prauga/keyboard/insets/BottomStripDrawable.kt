/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.insets

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class BottomStripDrawable(color: Int, private val height: Int) : Drawable() {

    private val paint = Paint().apply { this.color = color }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        canvas.drawRect(
            bounds.left.toFloat(),
            (bounds.bottom - height).toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            paint,
        )
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
