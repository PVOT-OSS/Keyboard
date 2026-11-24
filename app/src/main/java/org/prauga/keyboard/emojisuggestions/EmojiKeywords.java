/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.emojisuggestions;

import android.content.Context;
import android.util.Log;

import org.prauga.keyboard.utils.ExecutorUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EmojiKeywords {
    private static final String TAG = EmojiKeywords.class.getSimpleName();
    private static final String ENGLISH_ASSET = "emoji/keywords-en.tsv";
    private static final String ENGLISH = "en";
    private static final EmojiKeywords INSTANCE = new EmojiKeywords();

    private volatile Map<String, String> mEnglish = Collections.emptyMap();
    private boolean mLoading;

    private EmojiKeywords() {
    }

    @Nonnull
    public static EmojiKeywords getInstance() {
        return INSTANCE;
    }

    public synchronized void load(@Nonnull final Context context) {
        if (mLoading) {
            return;
        }
        mLoading = true;
        final Context appContext = context.getApplicationContext();
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute(() -> {
            try (Reader reader = new InputStreamReader(appContext.getAssets().open(ENGLISH_ASSET),
                    StandardCharsets.UTF_8)) {
                mEnglish = parse(reader);
            } catch (final IOException e) {
                Log.w(TAG, "Cannot read emoji keywords", e);
            }
        });
    }

    @Nullable
    public String lookup(@Nonnull final String word, @Nullable final Locale locale) {
        if (locale == null || !ENGLISH.equals(locale.getLanguage())) {
            return null;
        }
        return mEnglish.get(word.toLowerCase(Locale.ROOT));
    }

    @Nonnull
    static Map<String, String> parse(@Nonnull final Reader reader) throws IOException {
        final Map<String, String> keywords = new HashMap<>();
        final BufferedReader lines = new BufferedReader(reader);
        String line;
        while ((line = lines.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            final int tab = line.indexOf('\t');
            if (tab > 0 && tab < line.length() - 1) {
                keywords.put(line.substring(0, tab), line.substring(tab + 1));
            }
        }
        return Collections.unmodifiableMap(keywords);
    }
}
