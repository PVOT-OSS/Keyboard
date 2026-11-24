/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.onehanded;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OneHandedModeTest {
    @Test
    public void valuesRoundTrip() {
        for (final int mode : new int[] { OneHandedMode.OFF, OneHandedMode.LEFT,
                OneHandedMode.RIGHT }) {
            assertEquals(mode, OneHandedMode.fromValue(OneHandedMode.toValue(mode)));
        }
    }

    @Test
    public void unknownValuesMeanOff() {
        assertEquals(OneHandedMode.OFF, OneHandedMode.fromValue(null));
        assertEquals(OneHandedMode.OFF, OneHandedMode.fromValue("middle"));
    }

    @Test
    public void switchingSidesAlternates() {
        assertEquals(OneHandedMode.RIGHT, OneHandedMode.otherSide(OneHandedMode.LEFT));
        assertEquals(OneHandedMode.LEFT, OneHandedMode.otherSide(OneHandedMode.RIGHT));
    }

    @Test
    public void turningOnDefaultsToTheRight() {
        assertEquals(OneHandedMode.RIGHT, OneHandedMode.sideOrDefault(OneHandedMode.OFF));
        assertEquals(OneHandedMode.LEFT, OneHandedMode.sideOrDefault(OneHandedMode.LEFT));
    }

    @Test
    public void keyboardWidthIsAFractionOfTheScreen() {
        assertEquals(864, OneHandedMode.keyboardWidth(1080, 0.8f));
    }
}
