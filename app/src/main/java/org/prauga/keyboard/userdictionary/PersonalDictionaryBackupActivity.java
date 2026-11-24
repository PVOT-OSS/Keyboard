/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.UserDictionary;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.prauga.keyboard.R;
import org.prauga.keyboard.utils.ExecutorUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PersonalDictionaryBackupActivity extends ComponentActivity {
    private static final String TAG = PersonalDictionaryBackupActivity.class.getSimpleName();
    static final String ACTION_EXPORT = "org.prauga.keyboard.action.EXPORT_PERSONAL_DICTIONARY";
    static final String ACTION_IMPORT = "org.prauga.keyboard.action.IMPORT_PERSONAL_DICTIONARY";
    private static final String MIME_TYPE = "text/tab-separated-values";
    private static final String[] IMPORT_MIME_TYPES = { "text/*" };
    private static final String[] PROJECTION = {
            UserDictionary.Words.WORD,
            UserDictionary.Words.FREQUENCY,
            UserDictionary.Words.LOCALE,
            UserDictionary.Words.SHORTCUT
    };

    private final ActivityResultLauncher<String> mExportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(MIME_TYPE), this::onExportUri);
    private final ActivityResultLauncher<String[]> mImportLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), this::onImportUri);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            return;
        }
        final String action = getIntent().getAction();
        if (ACTION_EXPORT.equals(action)) {
            mExportLauncher.launch(getString(R.string.personal_dictionary_file_name));
        } else if (ACTION_IMPORT.equals(action)) {
            mImportLauncher.launch(IMPORT_MIME_TYPES);
        } else {
            finish();
        }
    }

    private void onExportUri(@Nullable final Uri uri) {
        if (uri != null) {
            final Context appContext = getApplicationContext();
            ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD)
                    .execute(() -> export(appContext, uri));
        }
        finish();
    }

    private void onImportUri(@Nullable final Uri uri) {
        if (uri != null) {
            final Context appContext = getApplicationContext();
            ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD)
                    .execute(() -> importFrom(appContext, uri));
        }
        finish();
    }

    private static void export(@Nonnull final Context context, @Nonnull final Uri uri) {
        final ContentResolver resolver = context.getContentResolver();
        int count = 0;
        try (Cursor cursor = resolver.query(UserDictionary.Words.CONTENT_URI, PROJECTION,
                null, null, UserDictionary.Words.WORD);
                OutputStream stream = resolver.openOutputStream(uri, "wt")) {
            if (cursor == null || stream == null) {
                throw new IOException("Cannot open the personal dictionary or the file");
            }
            final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(stream, StandardCharsets.UTF_8));
            while (cursor.moveToNext()) {
                final PersonalDictionaryEntry entry = fromCursor(cursor);
                if (entry.mWord.indexOf('\t') >= 0) {
                    continue;
                }
                writer.write(entry.toLine());
                writer.write('\n');
                count++;
            }
            writer.flush();
        } catch (final IOException | SecurityException e) {
            Log.w(TAG, "Personal dictionary export failed", e);
            showToast(context, context.getString(R.string.personal_dictionary_export_failed));
            return;
        }
        showToast(context, context.getResources().getQuantityString(
                R.plurals.personal_dictionary_exported, count, count));
    }

    private static void importFrom(@Nonnull final Context context, @Nonnull final Uri uri) {
        final ContentResolver resolver = context.getContentResolver();
        final List<ContentValues> newWords = new ArrayList<>();
        try (Cursor cursor = resolver.query(UserDictionary.Words.CONTENT_URI, PROJECTION,
                null, null, null);
                InputStream stream = resolver.openInputStream(uri)) {
            if (stream == null) {
                throw new IOException("Cannot open the file");
            }
            final Set<String> knownKeys = new HashSet<>();
            while (cursor != null && cursor.moveToNext()) {
                knownKeys.add(fromCursor(cursor).key());
            }
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                final PersonalDictionaryEntry entry = PersonalDictionaryEntry.fromLine(line);
                if (entry != null && knownKeys.add(entry.key())) {
                    newWords.add(toValues(entry));
                }
            }
            if (!newWords.isEmpty()) {
                resolver.bulkInsert(UserDictionary.Words.CONTENT_URI,
                        newWords.toArray(new ContentValues[0]));
            }
        } catch (final IOException | SecurityException | IllegalArgumentException e) {
            Log.w(TAG, "Personal dictionary import failed", e);
            showToast(context, context.getString(R.string.personal_dictionary_import_failed));
            return;
        }
        showToast(context, context.getResources().getQuantityString(
                R.plurals.personal_dictionary_imported, newWords.size(), newWords.size()));
    }

    @Nonnull
    private static PersonalDictionaryEntry fromCursor(@Nonnull final Cursor cursor) {
        final String word = cursor.getString(0);
        return new PersonalDictionaryEntry(word == null ? "" : word, cursor.getInt(1),
                cursor.getString(2), cursor.getString(3));
    }

    @Nonnull
    private static ContentValues toValues(@Nonnull final PersonalDictionaryEntry entry) {
        final ContentValues values = new ContentValues();
        values.put(UserDictionary.Words.WORD, entry.mWord);
        values.put(UserDictionary.Words.FREQUENCY, entry.mFrequency);
        values.put(UserDictionary.Words.LOCALE, entry.mLocale.isEmpty() ? null : entry.mLocale);
        values.put(UserDictionary.Words.APP_ID, 0);
        values.put(UserDictionary.Words.SHORTCUT, entry.mShortcut);
        return values;
    }

    private static void showToast(@Nonnull final Context context, @Nonnull final String text) {
        new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
    }
}
