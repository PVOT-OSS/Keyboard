/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.onehanded;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.prauga.keyboard.R;

import javax.annotation.Nonnull;

public final class OneHandedSideView extends LinearLayout {
    public interface Listener {
        void onOneHandedSwitchSide();

        void onOneHandedExit();
    }

    private final Rect mKeyboardRect = new Rect();
    private View mKeyboardView;
    private ImageButton mSwitchSide;
    private ImageButton mExit;
    private int mMode = OneHandedMode.OFF;
    private int mSideWidth;

    public OneHandedSideView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSwitchSide = findViewById(R.id.one_handed_switch_side);
        mExit = findViewById(R.id.one_handed_exit);
    }

    public void bind(@Nonnull final View keyboardView, @Nonnull final Listener listener) {
        mKeyboardView = keyboardView;
        mSwitchSide.setOnClickListener(view -> listener.onOneHandedSwitchSide());
        mExit.setOnClickListener(view -> listener.onOneHandedExit());
        keyboardView.getViewTreeObserver().addOnGlobalLayoutListener(this::syncWithKeyboard);
    }

    public void setMode(final int mode, final int sideWidth, @Nonnull final ColorStateList tint) {
        mMode = mode;
        mSideWidth = sideWidth;
        mSwitchSide.setImageResource(mode == OneHandedMode.LEFT
                ? R.drawable.ic_editing_chevron_right : R.drawable.ic_editing_chevron_left);
        mSwitchSide.setImageTintList(tint);
        mExit.setImageTintList(tint);
        syncWithKeyboard();
    }

    private void syncWithKeyboard() {
        if (mKeyboardView == null || mMode == OneHandedMode.OFF || mSideWidth <= 0
                || !mKeyboardView.isShown()) {
            if (getVisibility() != GONE) {
                setVisibility(GONE);
            }
            return;
        }
        mKeyboardRect.set(0, 0, mKeyboardView.getWidth(), mKeyboardView.getHeight());
        ((ViewGroup) getParent()).offsetDescendantRectToMyCoords(mKeyboardView, mKeyboardRect);
        final int left = mMode == OneHandedMode.LEFT
                ? mKeyboardRect.right - mSideWidth : mKeyboardRect.left;
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        if (params.width != mSideWidth || params.height != mKeyboardRect.height()
                || params.leftMargin != left || params.topMargin != mKeyboardRect.top) {
            params.width = mSideWidth;
            params.height = mKeyboardRect.height();
            params.leftMargin = left;
            params.topMargin = mKeyboardRect.top;
            params.gravity = Gravity.TOP | Gravity.LEFT;
            setLayoutParams(params);
        }
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
    }
}
