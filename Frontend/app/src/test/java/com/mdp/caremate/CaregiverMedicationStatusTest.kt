package com.mdp.caremate

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.repositories.MedRepository
import com.mdp.caremate.data.sources.local.MedicationAlarmScheduler
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.dashboard.DashboardViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaregiverMedicationStatusTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockApp: Application = mockk(relaxed = true)
    private val mockMedRepository: MedRepository = mockk(relaxed = true)
    private val mockFirebaseSource: FirebaseSource = mockk(relaxed = true)
    private val mockScheduler: MedicationAlarmScheduler = mockk(relaxed = true)

    private val dummyMedication = Medication(
        id = "med_123",
        name = "Amlodipine",
        dosage = "10mg",
        intakeHour = 8,
        intakeMinute = 0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateMedicationTakenStatus_nonPremiumUserWithPhoto_rejectsUpload() = runTest {
        val nonPremiumUser = User(uid = "caregiver_1", role = "caregiver", isPremium = false)
        coEvery { mockFirebaseSource.getCurrentUser() } returns Result.success(nonPremiumUser)

        val viewModel = DashboardViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMedicationTakenStatus(dummyMedication, isTakenToday = true, photoUrl = "base64_photo_string")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Fitur upload foto bukti minum obat hanya untuk akun Caregiver Premium!",
            viewModel.statusMessage.value
        )
    }

    @Test
    fun updateMedicationTakenStatus_premiumUserWithPhoto_successMessage() = runTest {
        val premiumUser = User(uid = "caregiver_2", role = "caregiver", isPremium = true)
        coEvery { mockFirebaseSource.getCurrentUser() } returns Result.success(premiumUser)

        val viewModel = DashboardViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMedicationTakenStatus(dummyMedication, isTakenToday = true, photoUrl = "base64_photo_string")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Bukti foto berhasil diunggah (Premium)",
            viewModel.statusMessage.value
        )
    }

    @Test
    fun updateMedicationTakenStatus_withoutPhoto_successMessage() = runTest {
        val user = User(uid = "caregiver_3", role = "caregiver", isPremium = false)
        coEvery { mockFirebaseSource.getCurrentUser() } returns Result.success(user)

        val viewModel = DashboardViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMedicationTakenStatus(dummyMedication, isTakenToday = true, photoUrl = null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Status obat diperbarui: Sudah Diminum",
            viewModel.statusMessage.value
        )
    }

    @Test
    fun updateMedicationTakenStatus_markUntaken_successMessage() = runTest {
        val user = User(uid = "caregiver_4", role = "caregiver", isPremium = false)
        coEvery { mockFirebaseSource.getCurrentUser() } returns Result.success(user)

        val viewModel = DashboardViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMedicationTakenStatus(dummyMedication, isTakenToday = false, photoUrl = null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Status obat diperbarui: Belum Diminum",
            viewModel.statusMessage.value
        )
    }
}
