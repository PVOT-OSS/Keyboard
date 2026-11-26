/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.prauga.keyboard.clipboard.ClipKinds.Kind

class ClipKindsTest {
    @Test
    fun recognisesEmailLinksAndPhoneNumbers() {
        assertEquals(Kind.EMAIL, ClipKinds.kindOf("someone@example.org"))
        assertEquals(Kind.LINK, ClipKinds.kindOf("https://example.org/a?b=c"))
        assertEquals(Kind.LINK, ClipKinds.kindOf("www.example.org"))
        assertEquals(Kind.LINK, ClipKinds.kindOf("example.co.uk/path"))
        assertEquals(Kind.PHONE, ClipKinds.kindOf("+44 20 7946 0958"))
        assertEquals(Kind.PHONE, ClipKinds.kindOf("(555) 123-4567"))
    }

    @Test
    fun leavesOrdinaryTextAndCodesAlone() {
        assertNull(ClipKinds.kindOf("hello world"))
        assertNull(ClipKinds.kindOf("482913"))
        assertNull(ClipKinds.kindOf("3.14"))
        assertNull(ClipKinds.kindOf("see example.org for details"))
    }
}
