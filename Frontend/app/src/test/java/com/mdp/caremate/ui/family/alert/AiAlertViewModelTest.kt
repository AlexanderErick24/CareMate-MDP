package com.mdp.caremate.ui.family.alert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.AiAlert
import com.mdp.caremate.data.repositories.PremiumRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiAlertViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: PremiumRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ====================================
    // TEST 1: fetchAlerts berhasil
    // ====================================
    @Test
    fun fetchAlerts_success_updatesAlertsLiveData() = runTest {
        // GIVEN: Firestore mengembalikan 2 alert
        val dummyAlerts = listOf(
            AiAlert(
                id = "alert_1",
                caregiverId = "cg_001",
                title = "Medication Verified",
                description = "Paracetamol 500mg sesuai.",
                severity = "SAFE",
                timestamp = 1720000000000L
            ),
            AiAlert(
                id = "alert_2",
                caregiverId = "cg_001",
                title = "Wrong Medication",
                description = "Obat tidak sesuai resep.",
                severity = "HIGH",
                timestamp = 1720000060000L
            )
        )
        coEvery { mockRepository.getAiAlertsForFamily() } returns Result.success(dummyAlerts)

        // WHEN: ViewModel memanggil fetchAlerts
        val viewModel = AiAlertViewModel(mockRepository)
        viewModel.fetchAlerts()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: LiveData alerts harus berisi 2 item
        val result = viewModel.alerts.value
        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertEquals("alert_1", result[0].id)
        assertEquals("alert_2", result[1].id)
    }

    // ====================================
    // TEST 2: fetchAlerts gagal (error)
    // ====================================
    @Test
    fun fetchAlerts_failure_updatesErrorMessage() = runTest {
        // GIVEN: Firestore mengembalikan error
        coEvery { mockRepository.getAiAlertsForFamily() } returns Result.failure(
            Exception("Network error")
        )

        // WHEN: ViewModel memanggil fetchAlerts
        val viewModel = AiAlertViewModel(mockRepository)
        viewModel.fetchAlerts()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: errorMessage harus terisi, alerts harus null/kosong
        val error = viewModel.errorMessage.value
        assertNotNull(error)
        assertTrue(error!!.contains("Network error"))
        assertNull(viewModel.alerts.value)
    }

    // ====================================
    // TEST 3: fetchAlerts kosong (0 alerts)
    // ====================================
    @Test
    fun fetchAlerts_emptyList_returnsEmptyList() = runTest {
        // GIVEN: Firestore mengembalikan list kosong
        coEvery { mockRepository.getAiAlertsForFamily() } returns Result.success(emptyList())

        // WHEN
        val viewModel = AiAlertViewModel(mockRepository)
        viewModel.fetchAlerts()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: alerts berisi list kosong (bukan null)
        val result = viewModel.alerts.value
        assertNotNull(result)
        assertTrue(result!!.isEmpty())
    }

    // ====================================
    // TEST 4: isLoading berubah saat fetch
    // ====================================
    @Test
    fun fetchAlerts_togglesLoadingState() = runTest {
        coEvery { mockRepository.getAiAlertsForFamily() } returns Result.success(emptyList())

        val viewModel = AiAlertViewModel(mockRepository)

        // Sebelum fetch, isLoading belum di-set
        assertNull(viewModel.isLoading.value)

        viewModel.fetchAlerts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Setelah selesai, isLoading harus false
        assertEquals(false, viewModel.isLoading.value)
    }
}
