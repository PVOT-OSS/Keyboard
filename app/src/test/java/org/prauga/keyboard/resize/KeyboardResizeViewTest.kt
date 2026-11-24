/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.resize

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardResizeViewTest {
    @Test
    fun scaleIsClampedToTheSettingRange() {
        assertEquals(KeyboardResizeView.MIN_SCALE, KeyboardResizeView.roundToStep(0.1f), 0.0001f)
        assertEquals(KeyboardResizeView.MAX_SCALE, KeyboardResizeView.roundToStep(3f), 0.0001f)
    }

    @Test
    fun scaleMovesInWholePercents() {
        assertEquals(0.93f, KeyboardResizeView.roundToStep(0.9271f), 0.0001f)
    }
}
