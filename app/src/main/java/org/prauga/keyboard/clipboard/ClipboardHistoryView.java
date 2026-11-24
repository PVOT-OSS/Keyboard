/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.prauga.keyboard.R;
import org.prauga.keyboard.suggestions.InlineSuggestionsStrip;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ClipboardHistoryView extends LinearLayout {
    public interface Listener {
        void onClipboardHistoryPaste(@Nonnull String text);

        void onClipboardHistoryClose();
    }

    private static final int SELECTED_ALPHA = 0x66;

    private ClipboardHistoryManager mManager;
    private Listener mListener;
    private ColorStateList mTint;
    private GridLayout mItems;
    private TextView mEmpty;
    private TextView mBack;
    private TextView mTitle;
    private TextView mClear;
    private View mActions;
    private TextView mPin;
    private TextView mDelete;
    private TextView mCancel;
    @Nullable
    private ClipboardHistory.Item mSelected;

    public ClipboardHistoryView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mItems = findViewById(R.id.clipboard_history_items);
        mEmpty = findViewById(R.id.clipboard_history_empty);
        mBack = findViewById(R.id.clipboard_history_back);
        mTitle = findViewById(R.id.clipboard_history_title);
        mClear = findViewById(R.id.clipboard_history_clear);
        mBack.setOnClickListener(view -> mListener.onClipboardHistoryClose());
        mClear.setOnClickListener(view -> {
            mManager.clearUnpinned();
            refresh();
        });
        mActions = findViewById(R.id.clipboard_history_actions);
        mPin = findViewById(R.id.clipboard_history_pin);
        mDelete = findViewById(R.id.clipboard_history_delete);
        mCancel = findViewById(R.id.clipboard_history_cancel);
        mPin.setOnClickListener(view -> {
            if (mSelected != null) {
                mManager.setPinned(mSelected.mText, !mSelected.mPinned);
            }
            select(null);
        });
        mDelete.setOnClickListener(view -> {
            if (mSelected != null) {
                mManager.remove(mSelected.mText);
            }
            select(null);
        });
        mCancel.setOnClickListener(view -> select(null));
    }

    public void setUp(@Nonnull final ClipboardHistoryManager manager,
            @Nonnull final Listener listener) {
        mManager = manager;
        mListener = listener;
    }

    public void show(@Nonnull final CharSequence backLabel, @Nonnull final ColorStateList tint) {
        mTint = tint;
        mBack.setText(backLabel);
        mBack.setTextColor(tint);
        mTitle.setTextColor(tint);
        mClear.setTextColor(tint);
        mEmpty.setTextColor(tint);
        mPin.setTextColor(tint);
        mDelete.setTextColor(tint);
        mCancel.setTextColor(tint);
        select(null);
    }

    private void select(@Nullable final ClipboardHistory.Item item) {
        mSelected = item;
        mActions.setVisibility(item == null ? GONE : VISIBLE);
        if (item != null) {
            mPin.setText(item.mPinned
                    ? R.string.clipboard_history_unpin : R.string.clipboard_history_pin);
        }
        refresh();
    }

    private void refresh() {
        mItems.removeAllViews();
        final List<ClipboardHistory.Item> items = mManager.getItems();
        mEmpty.setVisibility(items.isEmpty() ? VISIBLE : GONE);
        boolean hasUnpinned = false;
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        for (final ClipboardHistory.Item item : items) {
            hasUnpinned |= !item.mPinned;
            final TextView view = (TextView) inflater.inflate(
                    R.layout.clipboard_history_item, mItems, false);
            view.setText(item.mText);
            view.setTextColor(mTint);
            final int chipColor =
                    InlineSuggestionsStrip.getChipBackgroundColor(mTint.getDefaultColor());
            view.setBackgroundTintList(ColorStateList.valueOf(isSelected(item)
                    ? ColorUtils.setAlphaComponent(mTint.getDefaultColor(), SELECTED_ALPHA)
                    : chipColor));
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, item.mPinned ? R.drawable.ic_clipboard_pin : 0, 0);
            view.setCompoundDrawableTintList(mTint);
            view.setOnClickListener(v -> mListener.onClipboardHistoryPaste(item.mText));
            view.setOnLongClickListener(v -> {
                select(item);
                return true;
            });
            mItems.addView(view);
        }
        mClear.setVisibility(hasUnpinned ? VISIBLE : INVISIBLE);
    }

    private boolean isSelected(@Nonnull final ClipboardHistory.Item item) {
        return mSelected != null && mSelected.mText.equals(item.mText);
    }
}
