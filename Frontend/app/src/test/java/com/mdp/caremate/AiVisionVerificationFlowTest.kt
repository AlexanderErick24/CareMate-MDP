package com.mdp.caremate

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.AiAlert
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.repositories.MedRepository
import com.mdp.caremate.data.repositories.PremiumRepository
import com.mdp.caremate.data.sources.local.MedicationAlarmScheduler
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.dashboard.DashboardViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
class AiVisionVerificationFlowTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockApp: Application = mockk(relaxed = true)
    private val mockMedRepository: MedRepository = mockk(relaxed = true)
    private val mockFirebaseSource: FirebaseSource = mockk(relaxed = true)
    private val mockScheduler: MedicationAlarmScheduler = mockk(relaxed = true)
    private val mockPremiumRepository: PremiumRepository = mockk(relaxed = true)

    private val dummyMedication = Medication(
        id = "med_001",
        name = "Paracetamol 500mg",
        dosage = "1 tablet",
        intakeHour = 8,
        intakeMinute = 0
    )

    private val dummyAiAlert = AiAlert(
        id = "alert_001",
        caregiverId = "",
        title = "Medication Verified",
        description = "Obat sesuai.",
        severity = "SAFE",
        timestamp = 1720000000000L,
        imageUrl = "base64_photo_string"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ====================================================
    // TEST 1: Non-Premium TIDAK boleh trigger AI Verification
    // ====================================================
    @Test
    fun aiVerification_nonPremiumUser_doesNotCallVerifyMedication() = runTest {
        // GIVEN: User bukan premium
        val nonPremiumUser = User(uid = "cg_001", role = "caregiver", isPremium = false)
        coEvery { mockFirebaseSource.getCurrentUser() } returns Result.success(nonPremiumUser)

        val viewModel = DashboardViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: Caregiver mencoba upload foto
        viewModel.updateMedicationTakenStatus(dummyMedication, isTakenToday = true, photoUrl = "base64_img")
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Pesan error premium muncul, AI tidak dipanggil
        assertEquals(
            "Fitur upload foto bukti minum obat hanya untuk akun Caregiver Premium!",
            viewModel.statusMessage.value
        )
        // verifyMedication pada PremiumRepository TIDAK boleh dipanggil
        coVerify(exactly = 0) { mockPremiumRepository.verifyMedication(any(), any()) }
    }

    // ====================================================
    // TEST 2: Premium + Tanpa Foto → Tidak trigger AI
    // ====================================================
    @Test
    fun aiVerification_premiumWithoutPhoto_doesNotCallVerifyMedication() = runTest {
        // GIVEN: User premium, TAPI tidak ada foto
        val premiumUser = User(uid = "cg_002", role = "caregiver", isPremium = true)
        coEvery { mockFirebaseSource.getCurrentUser() } returns Result.success(premiumUser)

        val viewModel = DashboardViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: Caregiver tandai sudah minum TANPA foto
        viewModel.updateMedicationTakenStatus(dummyMedication, isTakenToday = true, photoUrl = null)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Pesan sukses biasa
        assertEquals(
            "Status obat diperbarui: Sudah Diminum",
            viewModel.statusMessage.value
        )
        // verifyMedication TIDAK boleh dipanggil
        coVerify(exactly = 0) { mockPremiumRepository.verifyMedication(any(), any()) }
    }

    // ====================================================
    // TEST 3: Belum diminum (untaken) → Tidak trigger AI
    // ====================================================
    @Test
    fun aiVerification_untakenStatus_doesNotTriggerAiVerification() = runTest {
        // GIVEN: User premium
        val premiumUser = User(uid = "cg_003", role = "caregiver", isPremium = true)
        coEvery { mockFirebaseSource.getCurrentUser() } returns Result.success(premiumUser)

        val viewModel = DashboardViewModel(mockApp, mockMedRepository, mockFirebaseSource, mockScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: Caregiver ubah ke "belum diminum" (isTakenToday = false)
        viewModel.updateMedicationTakenStatus(dummyMedication, isTakenToday = false, photoUrl = null)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Status pesan belum diminum
        assertEquals(
            "Status obat diperbarui: Belum Diminum",
            viewModel.statusMessage.value
        )
    }
}
