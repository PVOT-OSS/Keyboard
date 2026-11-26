/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipImagesTest {
    @Test
    fun onlyPlainClipImageNamesAreServed() {
        assertTrue(ClipImages.isValidName("clip-1795711220046.png"))
        assertFalse(ClipImages.isValidName("../shared_prefs/x.xml"))
        assertFalse(ClipImages.isValidName("clip-1/../../x"))
        assertFalse(ClipImages.isValidName("clipboard_pins"))
    }
}
