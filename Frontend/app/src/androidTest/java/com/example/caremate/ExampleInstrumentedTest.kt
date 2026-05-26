package com.mdp.caremate // Pastikan baris pertama ini juga sudah benar ya!

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Ubah string di bawah ini menjadi persis seperti namespace/applicationId kamu
        assertEquals("com.mdp.caremate", appContext.packageName)
    }
}