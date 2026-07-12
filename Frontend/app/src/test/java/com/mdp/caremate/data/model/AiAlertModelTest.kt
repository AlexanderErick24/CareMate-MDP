package com.mdp.caremate.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiAlertModelTest {

    // ====================================
    // TEST 1: Default values
    // ====================================
    @Test
    fun aiAlert_defaultValues_areCorrect() {
        val alert = AiAlert()

        assertEquals("", alert.id)
        assertEquals("", alert.caregiverId)
        assertEquals("", alert.title)
        assertEquals("", alert.description)
        assertEquals("", alert.severity)
        assertEquals(0L, alert.timestamp)
        assertNull(alert.imageUrl)
    }

    // ====================================
    // TEST 2: copy() mengubah caregiverId saja
    // ====================================
    @Test
    fun aiAlert_copy_preservesOtherFields() {
        val original = AiAlert(
            id = "alert_1",
            caregiverId = "old_cg",
            title = "Verified",
            description = "OK",
            severity = "SAFE",
            timestamp = 999L,
            imageUrl = "base64data"
        )

        val copied = original.copy(caregiverId = "new_cg")

        // caregiverId harus berubah
        assertEquals("new_cg", copied.caregiverId)
        // Field lain harus tetap sama
        assertEquals("alert_1", copied.id)
        assertEquals("Verified", copied.title)
        assertEquals("OK", copied.description)
        assertEquals("SAFE", copied.severity)
        assertEquals(999L, copied.timestamp)
        assertEquals("base64data", copied.imageUrl)
    }

    // ====================================
    // TEST 3: Equality & hash
    // ====================================
    @Test
    fun aiAlert_equality_worksCorrectly() {
        val a = AiAlert(id = "1", severity = "HIGH")
        val b = AiAlert(id = "1", severity = "HIGH")
        val c = AiAlert(id = "2", severity = "HIGH")

        assertEquals(a, b)
        assertNotEquals(a, c)
        assertEquals(a.hashCode(), b.hashCode())
    }

    // ====================================
    // TEST 4: Severity values valid
    // ====================================
    @Test
    fun aiAlert_severityValues_areExpectedStrings() {
        val validSeverities = listOf("SAFE", "LOW", "MEDIUM", "HIGH")

        validSeverities.forEach { severity ->
            val alert = AiAlert(severity = severity)
            assertEquals(severity, alert.severity)
        }
    }
}
