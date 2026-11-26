/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.UserDictionary
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.IOException
import org.prauga.keyboard.R
import org.prauga.keyboard.utils.ExecutorUtils

class PersonalDictionaryBackupActivity : ComponentActivity() {
    private val exportLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_TYPE)) { uri ->
            onDocumentChosen(uri, ::export)
        }
    private val importLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            onDocumentChosen(uri, ::importFrom)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            return
        }
        when (intent.action) {
            ACTION_EXPORT ->
                exportLauncher.launch(getString(R.string.personal_dictionary_file_name))
            ACTION_IMPORT -> importLauncher.launch(IMPORT_MIME_TYPES)
            else -> finish()
        }
    }

    private fun onDocumentChosen(uri: Uri?, work: (Context, Uri) -> Unit) {
        if (uri != null) {
            val appContext = applicationContext
            ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
                work(appContext, uri)
            }
        }
        finish()
    }

    companion object {
        const val ACTION_EXPORT = "org.prauga.keyboard.action.EXPORT_PERSONAL_DICTIONARY"
        const val ACTION_IMPORT = "org.prauga.keyboard.action.IMPORT_PERSONAL_DICTIONARY"
        private const val TAG = "PersonalDictionaryBackup"
        private const val MIME_TYPE = "text/tab-separated-values"
        private val IMPORT_MIME_TYPES = arrayOf("text/*")
        private val PROJECTION =
            arrayOf(
                UserDictionary.Words.WORD,
                UserDictionary.Words.FREQUENCY,
                UserDictionary.Words.LOCALE,
                UserDictionary.Words.SHORTCUT,
            )

        private fun export(context: Context, uri: Uri) {
            val resolver = context.contentResolver
            val count =
                try {
                    val cursor =
                        resolver.query(
                            UserDictionary.Words.CONTENT_URI,
                            PROJECTION,
                            null,
                            null,
                            UserDictionary.Words.WORD,
                        ) ?: throw IOException("Cannot open the personal dictionary")
                    cursor.use {
                        val stream =
                            resolver.openOutputStream(uri, "wt")
                                ?: throw IOException("Cannot open the file")
                        stream.bufferedWriter().use { writer ->
                            var written = 0
                            while (cursor.moveToNext()) {
                                val entry = fromCursor(cursor)
                                if ('\t' in entry.word) {
                                    continue
                                }
                                writer.write(entry.toLine())
                                writer.write("\n")
                                written++
                            }
                            written
                        }
                    }
                } catch (e: IOException) {
                    Log.w(TAG, "Personal dictionary export failed", e)
                    showToast(
                        context,
                        context.getString(R.string.personal_dictionary_export_failed),
                    )
                    return
                } catch (e: SecurityException) {
                    Log.w(TAG, "Personal dictionary export failed", e)
                    showToast(
                        context,
                        context.getString(R.string.personal_dictionary_export_failed),
                    )
                    return
                }
            showToast(
                context,
                context.resources.getQuantityString(
                    R.plurals.personal_dictionary_exported,
                    count,
                    count,
                ),
            )
        }

        private fun importFrom(context: Context, uri: Uri) {
            val resolver = context.contentResolver
            val newWords =
                try {
                    val knownKeys = HashSet<String>()
                    resolver
                        .query(UserDictionary.Words.CONTENT_URI, PROJECTION, null, null, null)
                        ?.use { cursor ->
                            while (cursor.moveToNext()) {
                                knownKeys.add(fromCursor(cursor).key())
                            }
                        }
                    val stream =
                        resolver.openInputStream(uri) ?: throw IOException("Cannot open the file")
                    val values =
                        stream.bufferedReader().useLines { lines ->
                            lines
                                .mapNotNull { PersonalDictionaryEntry.fromLine(it) }
                                .filter { knownKeys.add(it.key()) }
                                .map(::toValues)
                                .toList()
                        }
                    if (values.isNotEmpty()) {
                        resolver.bulkInsert(UserDictionary.Words.CONTENT_URI, values.toTypedArray())
                    }
                    values.size
                } catch (e: IOException) {
                    Log.w(TAG, "Personal dictionary import failed", e)
                    showToast(
                        context,
                        context.getString(R.string.personal_dictionary_import_failed),
                    )
                    return
                } catch (e: SecurityException) {
                    Log.w(TAG, "Personal dictionary import failed", e)
                    showToast(
                        context,
                        context.getString(R.string.personal_dictionary_import_failed),
                    )
                    return
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Personal dictionary import failed", e)
                    showToast(
                        context,
                        context.getString(R.string.personal_dictionary_import_failed),
                    )
                    return
                }
            showToast(
                context,
                context.resources.getQuantityString(
                    R.plurals.personal_dictionary_imported,
                    newWords,
                    newWords,
                ),
            )
        }

        private fun fromCursor(cursor: Cursor) =
            PersonalDictionaryEntry(
                cursor.getString(cursor.getColumnIndexOrThrow(UserDictionary.Words.WORD)).orEmpty(),
                cursor.getInt(cursor.getColumnIndexOrThrow(UserDictionary.Words.FREQUENCY)),
                cursor.getString(cursor.getColumnIndexOrThrow(UserDictionary.Words.LOCALE)),
                cursor.getString(cursor.getColumnIndexOrThrow(UserDictionary.Words.SHORTCUT)),
            )

        private fun toValues(entry: PersonalDictionaryEntry) =
            ContentValues().apply {
                put(UserDictionary.Words.WORD, entry.word)
                put(UserDictionary.Words.FREQUENCY, entry.frequency)
                put(UserDictionary.Words.LOCALE, entry.locale.ifEmpty { null })
                put(UserDictionary.Words.APP_ID, 0)
                put(UserDictionary.Words.SHORTCUT, entry.shortcut)
            }

        private fun showToast(context: Context, text: String) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
