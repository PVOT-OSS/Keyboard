/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.editing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.prauga.keyboard.R;
import org.prauga.keyboard.suggestions.InlineSuggestionsStrip;

import javax.annotation.Nonnull;

public final class TextEditingView extends LinearLayout {
    public interface Listener {
        void onEditingKey(int keyCode, boolean extendSelection);

        void onEditingAction(int actionId);

        void onTextEditingClose();
    }

    private static final int SELECTED_ALPHA = 0x66;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Listener mListener;
    private ColorStateList mTint;
    private TextView mBack;
    private TextView mSelect;
    private boolean mSelecting;

    public TextEditingView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBack = findViewById(R.id.text_editing_back);
        mSelect = findViewById(R.id.text_editing_select);
        mBack.setOnClickListener(view -> mListener.onTextEditingClose());
        mSelect.setOnClickListener(view -> setSelecting(!mSelecting));
        setUpMoveKey(R.id.text_editing_left, KeyEvent.KEYCODE_DPAD_LEFT);
        setUpMoveKey(R.id.text_editing_right, KeyEvent.KEYCODE_DPAD_RIGHT);
        setUpMoveKey(R.id.text_editing_up, KeyEvent.KEYCODE_DPAD_UP);
        setUpMoveKey(R.id.text_editing_down, KeyEvent.KEYCODE_DPAD_DOWN);
        setUpMoveKey(R.id.text_editing_line_start, KeyEvent.KEYCODE_MOVE_HOME);
        setUpMoveKey(R.id.text_editing_line_end, KeyEvent.KEYCODE_MOVE_END);
        setUpRepeatingKey(findViewById(R.id.text_editing_delete),
                () -> mListener.onEditingKey(KeyEvent.KEYCODE_DEL, false));
        setUpAction(R.id.text_editing_select_all, android.R.id.selectAll, false);
        setUpAction(R.id.text_editing_copy, android.R.id.copy, true);
        setUpAction(R.id.text_editing_cut, android.R.id.cut, true);
        setUpAction(R.id.text_editing_paste, android.R.id.paste, true);
    }

    public void setListener(@Nonnull final Listener listener) {
        mListener = listener;
    }

    public void show(@Nonnull final CharSequence backLabel, @Nonnull final ColorStateList tint) {
        mTint = tint;
        mBack.setText(backLabel);
        mBack.setTextColor(tint);
        ((TextView) findViewById(R.id.text_editing_title)).setTextColor(tint);
        final ViewGroup keys = findViewById(R.id.text_editing_keys);
        for (int i = 0; i < keys.getChildCount(); i++) {
            final View key = keys.getChildAt(i);
            if (key instanceof TextView) {
                ((TextView) key).setTextColor(tint);
            } else if (key instanceof ImageButton) {
                ((ImageButton) key).setImageTintList(tint);
            }
        }
        setSelecting(false);
    }

    private void setSelecting(final boolean selecting) {
        mSelecting = selecting;
        final ViewGroup keys = findViewById(R.id.text_editing_keys);
        final int chipColor = InlineSuggestionsStrip.getChipBackgroundColor(mTint.getDefaultColor());
        for (int i = 0; i < keys.getChildCount(); i++) {
            final View key = keys.getChildAt(i);
            key.setBackgroundTintList(ColorStateList.valueOf(key == mSelect && selecting
                    ? ColorUtils.setAlphaComponent(mTint.getDefaultColor(), SELECTED_ALPHA)
                    : chipColor));
        }
        mSelect.setActivated(selecting);
    }

    private void setUpMoveKey(final int viewId, final int keyCode) {
        setUpRepeatingKey(findViewById(viewId),
                () -> mListener.onEditingKey(keyCode, mSelecting));
    }

    private void setUpAction(final int viewId, final int actionId, final boolean endsSelection) {
        findViewById(viewId).setOnClickListener(view -> {
            mListener.onEditingAction(actionId);
            if (endsSelection) {
                setSelecting(false);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpRepeatingKey(@Nonnull final View key, @Nonnull final Runnable action) {
        final int startDelay = getResources().getInteger(R.integer.config_key_repeat_start_timeout);
        final int interval = getResources().getInteger(R.integer.config_key_repeat_interval);
        final boolean[] repeated = new boolean[1];
        final Runnable repeat = new Runnable() {
            @Override
            public void run() {
                repeated[0] = true;
                action.run();
                mHandler.postDelayed(this, interval);
            }
        };
        key.setOnClickListener(view -> action.run());
        key.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    repeated[0] = false;
                    mHandler.postDelayed(repeat, startDelay);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mHandler.removeCallbacks(repeat);
                    if (repeated[0]) {
                        view.setPressed(false);
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}
