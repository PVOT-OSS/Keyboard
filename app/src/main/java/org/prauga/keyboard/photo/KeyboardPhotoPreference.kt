/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.photo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.AttributeSet
import androidx.preference.Preference
import org.prauga.keyboard.R

class KeyboardPhotoPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs),
    SharedPreferences.OnSharedPreferenceChangeListener {
    init {
        isPersistent = false
    }

    override fun onAttached() {
        super.onAttached()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        update()
    }

    override fun onDetached() {
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onDetached()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KeyboardPhoto.PREF_PHOTO_VERSION) {
            update()
        }
    }

    override fun onClick() {
        if (key == KEY_REMOVE) {
            KeyboardPhoto.remove(context)
        } else {
            context.startActivity(Intent(context, KeyboardPhotoActivity::class.java))
        }
    }

    private fun update() {
        val set = KeyboardPhoto.isSet(context)
        if (key == KEY_REMOVE) {
            isVisible = set
        } else {
            setSummary(if (set) R.string.keyboard_photo_set else R.string.keyboard_photo_summary)
        }
    }

    private companion object {
        const val KEY_REMOVE = "remove_keyboard_photo"
    }
}
