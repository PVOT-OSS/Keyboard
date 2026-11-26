/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.photo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class PhotoDrawable(private val bitmap: Bitmap, private val dimAlpha: Int) : Drawable() {
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val source = Rect()

    override fun draw(canvas: Canvas) {
        val target = bounds
        if (target.isEmpty) {
            return
        }
        val scale = maxOf(
            target.width().toFloat() / bitmap.width,
            target.height().toFloat() / bitmap.height,
        )
        val width = (target.width() / scale).toInt()
        val height = (target.height() / scale).toInt()
        val left = (bitmap.width - width) / 2
        val top = (bitmap.height - height) / 2
        source.set(left, top, left + width, top + height)
        canvas.drawBitmap(bitmap, source, target, paint)
        canvas.drawColor(Color.argb(dimAlpha, 0, 0, 0))
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun getConstantState(): ConstantState = State(bitmap, dimAlpha)

    private class State(private val bitmap: Bitmap, private val dimAlpha: Int) : ConstantState() {
        override fun newDrawable(): Drawable = PhotoDrawable(bitmap, dimAlpha)

        override fun getChangingConfigurations(): Int = 0
    }
}
