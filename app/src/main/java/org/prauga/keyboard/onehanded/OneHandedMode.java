/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.onehanded;

import android.content.SharedPreferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class OneHandedMode {
    public static final String PREF_ONE_HANDED_MODE = "pref_one_handed_mode";
    public static final String PREF_ONE_HANDED_SIDE = "pref_one_handed_side";

    public static final int OFF = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private static final String VALUE_OFF = "off";
    private static final String VALUE_LEFT = "left";
    private static final String VALUE_RIGHT = "right";

    private OneHandedMode() {
    }

    public static int read(@Nonnull final SharedPreferences prefs) {
        return fromValue(prefs.getString(PREF_ONE_HANDED_MODE, VALUE_OFF));
    }

    public static void toggle(@Nonnull final SharedPreferences prefs) {
        final int mode = read(prefs) == OFF
                ? sideOrDefault(fromValue(prefs.getString(PREF_ONE_HANDED_SIDE, VALUE_RIGHT)))
                : OFF;
        write(prefs, mode);
    }

    public static void switchSide(@Nonnull final SharedPreferences prefs) {
        write(prefs, otherSide(read(prefs)));
    }

    public static void turnOff(@Nonnull final SharedPreferences prefs) {
        write(prefs, OFF);
    }

    private static void write(@Nonnull final SharedPreferences prefs, final int mode) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_ONE_HANDED_MODE, toValue(mode));
        if (mode != OFF) {
            editor.putString(PREF_ONE_HANDED_SIDE, toValue(mode));
        }
        editor.apply();
    }

    static int fromValue(@Nullable final String value) {
        if (VALUE_LEFT.equals(value)) {
            return LEFT;
        }
        if (VALUE_RIGHT.equals(value)) {
            return RIGHT;
        }
        return OFF;
    }

    @Nonnull
    static String toValue(final int mode) {
        switch (mode) {
            case LEFT:
                return VALUE_LEFT;
            case RIGHT:
                return VALUE_RIGHT;
            default:
                return VALUE_OFF;
        }
    }

    static int otherSide(final int mode) {
        return mode == LEFT ? RIGHT : LEFT;
    }

    static int sideOrDefault(final int mode) {
        return mode == OFF ? RIGHT : mode;
    }

    public static int keyboardWidth(final int screenWidth, final float widthFraction) {
        return Math.round(screenWidth * widthFraction);
    }
}
