/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

object ClipImages {
    private const val TAG = "ClipImages"
    private const val DIRECTORY = "clip_images"
    private const val PREFIX = "clip-"
    private const val MAX_BYTES = 10L * 1024 * 1024
    private const val AUTHORITY_SUFFIX = ".clipimages"

    fun directory(context: Context): File = File(context.noBackupFilesDir, DIRECTORY)

    fun authority(context: Context): String = context.packageName + AUTHORITY_SUFFIX

    fun uriFor(context: Context, name: String): Uri =
        Uri.Builder().scheme("content").authority(authority(context)).appendPath(name).build()

    fun fileFor(context: Context, name: String): File? =
        if (isValidName(name)) File(directory(context), name) else null

    fun copy(context: Context, source: Uri, mimeType: String, timestamp: Long): String? {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "img"
        val name = "$PREFIX$timestamp.$extension"
        val dir = directory(context)
        if (!dir.isDirectory && !dir.mkdirs()) {
            Log.w(TAG, "Cannot create the clip image directory")
            return null
        }
        val target = File(dir, name)
        pending.add(name)
        val copied = copyInto(context, source, target)
        if (!copied) {
            target.delete()
            pending.remove(name)
        }
        return name.takeIf { copied }
    }

    private fun copyInto(context: Context, source: Uri, target: File): Boolean =
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> copyLimited(input, output) }
            } != null
        } catch (e: IOException) {
            Log.w(TAG, "Cannot copy a clip image", e)
            false
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot read a clip image", e)
            false
        }

    @Throws(IOException::class)
    private fun copyLimited(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)
        var total = 0L
        var read = input.read(buffer)
        while (read >= 0) {
            total += read
            if (total > MAX_BYTES) {
                throw IOException("Clip image is too large")
            }
            output.write(buffer, 0, read)
            read = input.read(buffer)
        }
    }

    fun deleteUnreferenced(context: Context, referenced: Set<String>) {
        directory(context).listFiles()?.forEach { file ->
            if (file.name !in referenced && file.name !in pending && !file.delete()) {
                Log.w(TAG, "Cannot delete an old clip image")
            }
        }
    }

    internal fun isValidName(name: String): Boolean =
        name.startsWith(PREFIX) && '/' !in name && !name.contains("..")

    private const val BUFFER_SIZE = 8192
    private val pending: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun markAdded(name: String) {
        pending.remove(name)
    }
}
