/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

class ClipboardHistoryStoreTest {
    @Test
    fun roundTripKeepsTextAndTimestamps() {
        val file = File(Files.createTempDirectory("pins").toFile(), "pins")
        val pinned = listOf(
            ClipboardHistory.Item("line one\nline\ttwo", 42, true),
            ClipboardHistory.Item("emoji 😀", 7, true),
        )
        ClipboardHistoryStore.write(file, pinned)
        assertEquals(pinned, ClipboardHistoryStore.read(file))
    }

    @Test
    fun shortcutsRoundTrip() {
        val file = File(Files.createTempDirectory("pins").toFile(), "pins")
        val pinned = listOf(ClipboardHistory.Item("address", 1, true, "addr"))
        ClipboardHistoryStore.write(file, pinned)
        assertEquals(pinned, ClipboardHistoryStore.read(file))
    }

    @Test
    fun imageClipsRoundTrip() {
        val file = File(Files.createTempDirectory("pins").toFile(), "pins")
        val pinned = listOf(ClipboardHistory.Item("clip-1.png", 1, true, null, "image/png"))
        ClipboardHistoryStore.write(file, pinned)
        assertEquals(pinned, ClipboardHistoryStore.read(file))
    }

    @Test
    fun versionTwoFilesStillLoad() {
        val file = Files.createTempFile("pins", null).toFile()
        file.writeText("pvot-clipboard-pins 2\n42\taGVsbG8=\tYQ==\n")
        assertEquals(
            listOf(ClipboardHistory.Item("hello", 42, true, "a")),
            ClipboardHistoryStore.read(file),
        )
    }

    @Test
    fun versionOneFilesStillLoad() {
        val file = Files.createTempFile("pins", null).toFile()
        file.writeText("pvot-clipboard-pins 1\n42\taGVsbG8=\n")
        assertEquals(listOf(ClipboardHistory.Item("hello", 42, true)), ClipboardHistoryStore.read(file))
    }

    @Test
    fun missingFileReadsEmpty() {
        val dir = Files.createTempDirectory("pins").toFile()
        assertTrue(ClipboardHistoryStore.read(File(dir, "none")).isEmpty())
    }

    @Test(expected = IOException::class)
    fun corruptFileIsRejected() {
        val file = Files.createTempFile("pins", null).toFile()
        file.writeText("something else\n")
        ClipboardHistoryStore.read(file)
    }
}
