/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import java.io.File
import java.io.IOException
import java.util.Base64

object ClipboardHistoryStore {
    private const val HEADER = "pvot-clipboard-pins 3"
    private const val HEADER_V2 = "pvot-clipboard-pins 2"
    private const val HEADER_V1 = "pvot-clipboard-pins 1"
    private const val SEPARATOR = "\t"
    private const val FIELD_TIMESTAMP = 0
    private const val FIELD_TEXT = 1
    private const val FIELD_SHORTCUT = 2
    private const val FIELD_MIME_TYPE = 3
    private val FIELD_COUNTS =
        mapOf(
            HEADER to FIELD_MIME_TYPE + 1,
            HEADER_V2 to FIELD_SHORTCUT + 1,
            HEADER_V1 to FIELD_TEXT + 1,
        )

    @Throws(IOException::class)
    fun read(file: File): List<ClipboardHistory.Item> {
        if (!file.exists()) {
            return emptyList()
        }
        val lines = file.readLines(Charsets.UTF_8)
        val fieldCount =
            FIELD_COUNTS[lines.firstOrNull()] ?: throw IOException("Unknown clipboard pins format")
        return lines.drop(1).filter { it.isNotEmpty() }.map { parseLine(it, fieldCount) }
    }

    private fun parseLine(line: String, fieldCount: Int): ClipboardHistory.Item {
        val fields = line.split(SEPARATOR)
        if (fields.size != fieldCount) {
            throw IOException("Malformed clipboard pin")
        }
        return try {
            ClipboardHistory.Item(
                decode(fields[FIELD_TEXT]),
                fields[FIELD_TIMESTAMP].toLong(),
                true,
                fields.getOrNull(FIELD_SHORTCUT)?.let(::decode)?.takeIf { it.isNotEmpty() },
                fields.getOrNull(FIELD_MIME_TYPE)?.let(::decode)?.takeIf { it.isNotEmpty() },
            )
        } catch (e: IllegalArgumentException) {
            throw IOException("Malformed clipboard pin", e)
        }
    }

    private fun encode(text: String): String =
        Base64.getEncoder().encodeToString(text.toByteArray())

    private fun decode(field: String): String =
        String(Base64.getDecoder().decode(field), Charsets.UTF_8)

    @Throws(IOException::class)
    fun write(file: File, pinnedItems: List<ClipboardHistory.Item>) {
        val temp = File(file.path + ".tmp")
        temp.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(HEADER)
            writer.write("\n")
            for (item in pinnedItems) {
                writer.write(item.timestamp.toString())
                writer.write(SEPARATOR)
                writer.write(encode(item.text))
                writer.write(SEPARATOR)
                writer.write(encode(item.shortcut.orEmpty()))
                writer.write(SEPARATOR)
                writer.write(encode(item.mimeType.orEmpty()))
                writer.write("\n")
            }
        }
        if (!temp.renameTo(file)) {
            throw IOException("Cannot replace ${file.name}")
        }
    }
}
