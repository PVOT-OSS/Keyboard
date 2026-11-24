/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.Nonnull;

public final class ClipboardHistoryStore {
    private static final String HEADER = "pvot-clipboard-pins 1";
    private static final String SEPARATOR = "\t";

    private ClipboardHistoryStore() {
    }

    @Nonnull
    public static List<ClipboardHistory.Item> read(@Nonnull final File file) throws IOException {
        final List<ClipboardHistory.Item> items = new ArrayList<>();
        if (!file.exists()) {
            return items;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            if (!HEADER.equals(reader.readLine())) {
                throw new IOException("Unknown clipboard pins format");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] fields = line.split(SEPARATOR, -1);
                if (fields.length != 2) {
                    throw new IOException("Malformed clipboard pin");
                }
                try {
                    final long timestamp = Long.parseLong(fields[0]);
                    final String text = new String(Base64.getDecoder().decode(fields[1]),
                            StandardCharsets.UTF_8);
                    items.add(new ClipboardHistory.Item(text, timestamp, true));
                } catch (final IllegalArgumentException e) {
                    throw new IOException("Malformed clipboard pin", e);
                }
            }
        }
        return items;
    }

    public static void write(@Nonnull final File file,
            @Nonnull final List<ClipboardHistory.Item> pinnedItems) throws IOException {
        final File temp = new File(file.getPath() + ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(temp),
                StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.write('\n');
            for (final ClipboardHistory.Item item : pinnedItems) {
                writer.write(Long.toString(item.mTimestamp));
                writer.write(SEPARATOR);
                writer.write(Base64.getEncoder().encodeToString(
                        item.mText.getBytes(StandardCharsets.UTF_8)));
                writer.write('\n');
            }
        }
        if (!temp.renameTo(file)) {
            throw new IOException("Cannot replace " + file.getName());
        }
    }
}
