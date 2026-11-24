/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.toolbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.prauga.keyboard.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class KeyboardToolbar extends LinearLayout {
    public interface Listener {
        void onToolbarKey(int code);
    }

    @Nullable
    private Listener mListener;

    public KeyboardToolbar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
    }

    public void setListener(@Nullable final Listener listener) {
        mListener = listener;
    }

    public void addKey(final int code, @DrawableRes final int iconResId,
            @Nonnull final ColorStateList tint, @StringRes final int contentDescriptionResId) {
        final ImageButton button =
                new ImageButton(getContext(), null, R.attr.suggestionWordStyle);
        button.setImageResource(iconResId);
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setImageTintList(tint);
        button.setContentDescription(getResources().getString(contentDescriptionResId));
        button.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onToolbarKey(code);
            }
        });
        addView(button, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
    }
}
