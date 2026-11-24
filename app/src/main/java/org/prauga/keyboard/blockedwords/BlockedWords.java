/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.blockedwords;

import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

public final class BlockedWords {
    public static final String PREF_BLOCKED_WORDS = "pref_blocked_words";

    private BlockedWords() {
    }

    @Nonnull
    public static Set<String> read(@Nonnull final SharedPreferences prefs) {
        final Set<String> stored = prefs.getStringSet(PREF_BLOCKED_WORDS, null);
        if (stored == null || stored.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new TreeSet<>(stored));
    }

    public static void add(@Nonnull final SharedPreferences prefs, @Nonnull final String word) {
        final String normalized = normalize(word);
        if (normalized.isEmpty()) {
            return;
        }
        final Set<String> words = new HashSet<>(read(prefs));
        if (words.add(normalized)) {
            prefs.edit().putStringSet(PREF_BLOCKED_WORDS, words).apply();
        }
    }

    public static void remove(@Nonnull final SharedPreferences prefs, @Nonnull final String word) {
        final Set<String> words = new HashSet<>(read(prefs));
        if (words.remove(normalize(word))) {
            prefs.edit().putStringSet(PREF_BLOCKED_WORDS, words).apply();
        }
    }

    public static boolean isBlocked(@Nonnull final Set<String> blockedWords,
            @Nonnull final String word) {
        return !blockedWords.isEmpty() && blockedWords.contains(normalize(word));
    }

    @Nonnull
    public static String normalize(@Nonnull final String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }
}
