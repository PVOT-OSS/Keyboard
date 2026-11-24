/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.userdictionary;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import androidx.preference.Preference;

public final class PersonalDictionaryBackupPreference extends Preference {
    private static final String KEY_EXPORT = "export_personal_dictionary";

    public PersonalDictionaryBackupPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
    }

    @Override
    protected void onClick() {
        final String action = KEY_EXPORT.equals(getKey())
                ? PersonalDictionaryBackupActivity.ACTION_EXPORT
                : PersonalDictionaryBackupActivity.ACTION_IMPORT;
        getContext().startActivity(
                new Intent(getContext(), PersonalDictionaryBackupActivity.class)
                        .setAction(action));
    }
}
