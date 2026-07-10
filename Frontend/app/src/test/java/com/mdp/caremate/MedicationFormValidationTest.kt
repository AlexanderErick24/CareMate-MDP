package com.mdp.caremate

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.repositories.MedRepository
import com.mdp.caremate.data.sources.local.MedicationAlarmScheduler
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.medicationform.MedFormViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationFormValidationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockApp: Application = mockk(relaxed = true)
    private val mockMedRepository: MedRepository = mockk(relaxed = true)
    private val mockFirebaseSource: FirebaseSource = mockk(relaxed = true)
    private val mockScheduler: MedicationAlarmScheduler = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MedFormViewModel {
        return MedFormViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
    }

    @Test
    fun saveMedication_emptyName_returnsError() {
        val viewModel = createViewModel()
        viewModel.saveMedication(
            name = "",
            dosage = "1 Tablet",
            hourText = "08",
            minuteText = "00"
        )
        assertEquals("Semua field wajib diisi.", viewModel.message.value)
    }

    @Test
    fun saveMedication_emptyDosage_returnsError() {
        val viewModel = createViewModel()
        viewModel.saveMedication(
            name = "Paracetamol",
            dosage = "",
            hourText = "08",
            minuteText = "00"
        )
        assertEquals("Semua field wajib diisi.", viewModel.message.value)
    }

    @Test
    fun saveMedication_invalidHour_returnsError() {
        val viewModel = createViewModel()
        viewModel.saveMedication(
            name = "Paracetamol",
            dosage = "1 Tablet",
            hourText = "25",
            minuteText = "00"
        )
        assertEquals("Jam harus 0-23 dan menit 0-59.", viewModel.message.value)
    }

    @Test
    fun saveMedication_invalidMinute_returnsError() {
        val viewModel = createViewModel()
        viewModel.saveMedication(
            name = "Paracetamol",
            dosage = "1 Tablet",
            hourText = "12",
            minuteText = "-5"
        )
        assertEquals("Jam harus 0-23 dan menit 0-59.", viewModel.message.value)
    }

    @Test
    fun saveMedication_nonNumericHour_returnsError() {
        val viewModel = createViewModel()
        viewModel.saveMedication(
            name = "Paracetamol",
            dosage = "1 Tablet",
            hourText = "pagi",
            minuteText = "00"
        )
        assertEquals("Semua field wajib diisi.", viewModel.message.value)
    }
}
