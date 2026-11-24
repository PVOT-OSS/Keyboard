/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.keytextsize

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyTextSizeTest {
    @Test
    fun sizesGrowFromSmallToLargest() {
        val scales = listOf("small", "default", "large", "largest").map(KeyTextSize::scaleOf)
        assertEquals(scales.sorted(), scales)
        assertEquals(1.0f, KeyTextSize.scaleOf("default"), 0f)
    }

    @Test
    fun unknownValuesUseTheDefault() {
        assertEquals(1.0f, KeyTextSize.scaleOf(null), 0f)
        assertEquals(1.0f, KeyTextSize.scaleOf("huge"), 0f)
    }
}
