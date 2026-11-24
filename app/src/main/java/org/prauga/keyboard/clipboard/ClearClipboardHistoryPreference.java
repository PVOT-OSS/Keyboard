/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.prauga.keyboard.R;

public final class ClearClipboardHistoryPreference extends Preference {
    public ClearClipboardHistoryPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
    }

    @Override
    protected void onClick() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.clear_clipboard_history_confirm_title)
                .setMessage(R.string.clear_clipboard_history_confirm_message)
                .setPositiveButton(R.string.clear_clipboard_history_confirm, (dialog, which) -> {
                    ClipboardHistoryManager.getInstance(getContext()).clearAll();
                    Toast.makeText(getContext(), R.string.clear_clipboard_history_done,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
