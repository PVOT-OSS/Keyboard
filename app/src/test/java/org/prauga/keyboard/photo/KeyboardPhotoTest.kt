/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.photo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardPhotoTest {
    @Test
    fun largePhotosAreSampledDownToTheMaximumEdge() {
        assertEquals(1, KeyboardPhoto.sampleSize(1080, 720, 1440))
        assertEquals(2, KeyboardPhoto.sampleSize(4000, 3000, 1440))
        assertEquals(4, KeyboardPhoto.sampleSize(6000, 4000, 1440))
    }

    @Test
    fun darkerSettingsDimMore() {
        assertTrue(KeyboardPhoto.dimAlpha("light") < KeyboardPhoto.dimAlpha("medium"))
        assertTrue(KeyboardPhoto.dimAlpha("medium") < KeyboardPhoto.dimAlpha("dark"))
        assertEquals(KeyboardPhoto.dimAlpha("medium"), KeyboardPhoto.dimAlpha(null))
    }
}
