/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import java.io.File
import java.io.IOException
import java.util.Base64

object ClipboardHistoryStore {
    private const val HEADER = "pvot-clipboard-pins 1"
    private const val SEPARATOR = "\t"

    @Throws(IOException::class)
    fun read(file: File): List<ClipboardHistory.Item> {
        if (!file.exists()) {
            return emptyList()
        }
        val lines = file.readLines(Charsets.UTF_8)
        if (lines.firstOrNull() != HEADER) {
            throw IOException("Unknown clipboard pins format")
        }
        return lines.drop(1).filter { it.isNotEmpty() }.map { line ->
            val fields = line.split(SEPARATOR)
            if (fields.size != 2) {
                throw IOException("Malformed clipboard pin")
            }
            try {
                ClipboardHistory.Item(
                    String(Base64.getDecoder().decode(fields[1]), Charsets.UTF_8),
                    fields[0].toLong(), true
                )
            } catch (e: IllegalArgumentException) {
                throw IOException("Malformed clipboard pin", e)
            }
        }
    }

    @Throws(IOException::class)
    fun write(file: File, pinnedItems: List<ClipboardHistory.Item>) {
        val temp = File(file.path + ".tmp")
        temp.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(HEADER)
            writer.write("\n")
            for (item in pinnedItems) {
                writer.write(item.timestamp.toString())
                writer.write(SEPARATOR)
                writer.write(Base64.getEncoder().encodeToString(item.text.toByteArray()))
                writer.write("\n")
            }
        }
        if (!temp.renameTo(file)) {
            throw IOException("Cannot replace ${file.name}")
        }
    }
}
