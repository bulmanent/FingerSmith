package com.fingersmith

import com.fingersmith.util.KeyboardLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {
    @Test
    fun midiToIndexMapping() {
        assertEquals(0, KeyboardLayout.midiToKeyIndex(48, 48, 37))
        assertEquals(36, KeyboardLayout.midiToKeyIndex(84, 48, 37))
        assertEquals(-1, KeyboardLayout.midiToKeyIndex(85, 48, 37))
    }

    @Test
    fun blackWhiteDetection() {
        assertFalse(KeyboardLayout.isBlack(60))
        assertTrue(KeyboardLayout.isBlack(61))
        assertFalse(KeyboardLayout.isBlack(64))
    }

    @Test
    fun noteNameFormatting() {
        assertEquals("C4", KeyboardLayout.noteName(60))
        assertEquals("A4", KeyboardLayout.noteName(69))
    }
}
