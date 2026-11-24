/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.onehanded

import org.junit.Assert.assertEquals
import org.junit.Test

class OneHandedModeTest {
    @Test
    fun valuesRoundTrip() {
        for (mode in listOf(OneHandedMode.OFF, OneHandedMode.LEFT, OneHandedMode.RIGHT)) {
            assertEquals(mode, OneHandedMode.fromValue(OneHandedMode.toValue(mode)))
        }
    }

    @Test
    fun unknownValuesMeanOff() {
        assertEquals(OneHandedMode.OFF, OneHandedMode.fromValue(null))
        assertEquals(OneHandedMode.OFF, OneHandedMode.fromValue("middle"))
    }

    @Test
    fun switchingSidesAlternates() {
        assertEquals(OneHandedMode.RIGHT, OneHandedMode.otherSide(OneHandedMode.LEFT))
        assertEquals(OneHandedMode.LEFT, OneHandedMode.otherSide(OneHandedMode.RIGHT))
    }

    @Test
    fun turningOnDefaultsToTheRight() {
        assertEquals(OneHandedMode.RIGHT, OneHandedMode.sideOrDefault(OneHandedMode.OFF))
        assertEquals(OneHandedMode.LEFT, OneHandedMode.sideOrDefault(OneHandedMode.LEFT))
    }

    @Test
    fun keyboardWidthIsAFractionOfTheScreen() {
        assertEquals(864, OneHandedMode.keyboardWidth(1080, 0.8f))
    }
}
