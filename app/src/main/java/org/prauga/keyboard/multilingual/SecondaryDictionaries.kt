/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.multilingual

import android.content.Context
import org.prauga.keyboard.Dictionary
import org.prauga.keyboard.DictionaryCollection
import org.prauga.keyboard.DictionaryFactory
import org.prauga.keyboard.utils.ExecutorUtils
import java.util.Locale

class SecondaryDictionaries {
    @Volatile
    private var loaded: List<DictionaryCollection> = emptyList()
    private var requested: List<Locale> = emptyList()

    val dictionaries: List<Dictionary>
        get() = loaded

    @Synchronized
    fun setLocales(context: Context, locales: List<Locale>) {
        if (locales == requested) {
            return
        }
        requested = locales
        val appContext = context.applicationContext
        ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
            val dictionaries = locales.map { DictionaryFactory.createMainDictionaryFromManager(appContext, it) }
                .filter { it.isInitialized }
            val old = synchronized(this) {
                if (requested != locales) {
                    dictionaries.forEach { it.close() }
                    return@execute
                }
                loaded.also { loaded = dictionaries }
            }
            old.forEach { it.close() }
        }
    }

    @Synchronized
    fun close() {
        requested = emptyList()
        val old = loaded
        loaded = emptyList()
        old.forEach { it.close() }
    }
}
