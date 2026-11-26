/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.photo

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.File
import java.io.IOException

object KeyboardPhoto {
    const val PREF_PHOTO_VERSION = "pref_keyboard_photo"
    const val PREF_PHOTO_DARKNESS = "pref_keyboard_photo_darkness"
    private const val TAG = "KeyboardPhoto"
    private const val FILE_NAME = "keyboard_photo.jpg"
    private const val MAX_EDGE = 1440
    private const val JPEG_QUALITY = 90

    private var cachedVersion = 0L
    private var cachedBitmap: Bitmap? = null

    @JvmStatic fun version(prefs: SharedPreferences): Long = prefs.getLong(PREF_PHOTO_VERSION, 0)

    @JvmStatic
    fun isSet(context: Context): Boolean =
        version(PreferenceManager.getDefaultSharedPreferences(context)) != 0L

    @JvmStatic
    @Synchronized
    fun newDrawable(context: Context): Drawable? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val version = version(prefs)
        if (version != 0L && (version != cachedVersion || cachedBitmap == null)) {
            cachedBitmap = BitmapFactory.decodeFile(file(context).path)
            cachedVersion = version
        }
        val bitmap = cachedBitmap?.takeIf { version != 0L } ?: return null
        return PhotoDrawable(bitmap, dimAlpha(prefs.getString(PREF_PHOTO_DARKNESS, null)))
    }

    fun save(context: Context, source: Uri): Boolean {
        val bitmap = readScaled(context, source)
        val saved = bitmap != null && write(context, bitmap)
        if (saved) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_PHOTO_VERSION, System.currentTimeMillis())
                .apply()
        }
        return saved
    }

    private fun readScaled(context: Context, source: Uri): Bitmap? =
        try {
            decodeScaled(context, source)
        } catch (e: IOException) {
            Log.w(TAG, "Cannot read the photo", e)
            null
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot read the photo", e)
            null
        }

    private fun write(context: Context, bitmap: Bitmap): Boolean =
        try {
            file(context).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)
            }
            true
        } catch (e: IOException) {
            Log.w(TAG, "Cannot save the photo", e)
            false
        }

    fun remove(context: Context) {
        if (!file(context).delete()) {
            Log.w(TAG, "Cannot delete the photo")
        }
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .remove(PREF_PHOTO_VERSION)
            .apply()
    }

    private const val DIM_LIGHT = 0x40
    private const val DIM_MEDIUM = 0x70
    private const val DIM_DARK = 0xA0

    internal fun dimAlpha(darkness: String?): Int =
        when (darkness) {
            "light" -> DIM_LIGHT
            "dark" -> DIM_DARK
            else -> DIM_MEDIUM
        }

    internal fun sampleSize(width: Int, height: Int, maxEdge: Int): Int {
        var sample = 1
        while (maxOf(width, height) / (sample * 2) >= maxEdge) {
            sample *= 2
        }
        return sample
    }

    private fun decodeScaled(context: Context, source: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: return null
        val options =
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_EDGE)
            }
        return resolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private fun file(context: Context) = File(context.noBackupFilesDir, FILE_NAME)
}
