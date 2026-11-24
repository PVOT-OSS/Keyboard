/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ClipboardHistoryStoreTest {
    @Test
    public void roundTripKeepsTextAndTimestamps() throws IOException {
        final File dir = Files.createTempDirectory("pins").toFile();
        final File file = new File(dir, "pins");
        final List<ClipboardHistory.Item> pinned = Arrays.asList(
                new ClipboardHistory.Item("line one\nline\ttwo", 42, true),
                new ClipboardHistory.Item("emoji 😀", 7, true));
        ClipboardHistoryStore.write(file, pinned);
        final List<ClipboardHistory.Item> read = ClipboardHistoryStore.read(file);
        assertEquals(2, read.size());
        assertEquals("line one\nline\ttwo", read.get(0).mText);
        assertEquals(42, read.get(0).mTimestamp);
        assertEquals("emoji 😀", read.get(1).mText);
        assertTrue(read.get(1).mPinned);
    }

    @Test
    public void missingFileReadsEmpty() throws IOException {
        final File dir = Files.createTempDirectory("pins").toFile();
        assertTrue(ClipboardHistoryStore.read(new File(dir, "none")).isEmpty());
    }

    @Test(expected = IOException.class)
    public void corruptFileIsRejected() throws IOException {
        final File file = Files.createTempFile("pins", null).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("something else\n");
        }
        ClipboardHistoryStore.read(file);
    }
}
