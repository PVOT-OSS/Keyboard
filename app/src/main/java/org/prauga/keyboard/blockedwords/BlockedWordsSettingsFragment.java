/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.blockedwords;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.prauga.keyboard.R;
import org.prauga.keyboard.settings.CustomPreferenceFragment;

import java.util.Set;

public final class BlockedWordsSettingsFragment extends CustomPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences mPrefs;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        mPrefs = getPreferenceManager().getSharedPreferences();
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(requireContext()));
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        refresh();
    }

    @Override
    public void onPause() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        if (BlockedWords.PREF_BLOCKED_WORDS.equals(key)) {
            refresh();
        }
    }

    private void refresh() {
        final Context context = requireContext();
        final PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        final Preference add = new Preference(context);
        add.setPersistent(false);
        add.setTitle(R.string.blocked_words_add);
        add.setIcon(R.drawable.ic_add_circle);
        add.setOnPreferenceClickListener(preference -> {
            showAddDialog();
            return true;
        });
        screen.addPreference(add);

        final Set<String> words = BlockedWords.read(mPrefs);
        if (words.isEmpty()) {
            final Preference empty = new Preference(context);
            empty.setPersistent(false);
            empty.setSelectable(false);
            empty.setSummary(R.string.blocked_words_empty);
            screen.addPreference(empty);
            return;
        }
        for (final String word : words) {
            final Preference preference = new Preference(context);
            preference.setPersistent(false);
            preference.setTitle(word);
            preference.setOnPreferenceClickListener(p -> {
                showUnblockDialog(word);
                return true;
            });
            screen.addPreference(preference);
        }
    }

    private void showAddDialog() {
        final View view = getLayoutInflater().inflate(R.layout.blocked_word_input, null);
        final EditText input = view.findViewById(R.id.blocked_word);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.blocked_words_add)
                .setView(view)
                .setPositiveButton(R.string.blocked_words_block,
                        (dialog, which) -> BlockedWords.add(mPrefs, input.getText().toString()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showUnblockDialog(final String word) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.blocked_words_unblock_title, word))
                .setPositiveButton(R.string.blocked_words_unblock,
                        (dialog, which) -> BlockedWords.remove(mPrefs, word))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
