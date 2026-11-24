/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PersonalDictionaryEntry {
    private static final char SEPARATOR = '\t';
    private static final String COMMENT_PREFIX = "#";
    private static final int MIN_FREQUENCY = 1;
    private static final int MAX_FREQUENCY = 255;
    private static final int MAX_LENGTH = 48;
    static final int DEFAULT_FREQUENCY = 250;

    @Nonnull
    public final String mWord;
    public final int mFrequency;
    @Nonnull
    public final String mLocale;
    @Nullable
    public final String mShortcut;

    public PersonalDictionaryEntry(@Nonnull final String word, final int frequency,
            @Nullable final String locale, @Nullable final String shortcut) {
        mWord = word;
        mFrequency = Math.max(MIN_FREQUENCY, Math.min(MAX_FREQUENCY, frequency));
        mLocale = locale == null ? "" : locale;
        mShortcut = shortcut == null || shortcut.isEmpty() ? null : shortcut;
    }

    @Nonnull
    public String toLine() {
        return mWord + SEPARATOR + mFrequency + SEPARATOR + mLocale + SEPARATOR
                + (mShortcut == null ? "" : mShortcut);
    }

    @Nonnull
    public String key() {
        return mWord + SEPARATOR + mLocale;
    }

    @Nullable
    public static PersonalDictionaryEntry fromLine(@Nonnull final String line) {
        if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
            return null;
        }
        final String[] fields = line.split(String.valueOf(SEPARATOR), -1);
        final String word = fields[0].trim();
        if (word.isEmpty() || word.length() > MAX_LENGTH) {
            return null;
        }
        int frequency = DEFAULT_FREQUENCY;
        if (fields.length > 1 && !fields[1].trim().isEmpty()) {
            try {
                frequency = Integer.parseInt(fields[1].trim());
            } catch (final NumberFormatException e) {
                return null;
            }
        }
        final String locale = fields.length > 2 ? fields[2].trim() : "";
        final String shortcut = fields.length > 3 ? fields[3].trim() : "";
        if (shortcut.length() > MAX_LENGTH) {
            return null;
        }
        return new PersonalDictionaryEntry(word, frequency, locale, shortcut);
    }
}
