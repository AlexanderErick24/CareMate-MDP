package com.mdp.caremate.ui.premium

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.repositories.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiJournalingTest {

    // Rule ini wajib agar LiveData berjalan sinkron
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Dispatcher buatan untuk menggantikan Dispatchers.Main pada Coroutine
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepository: PremiumRepository
    private lateinit var viewModel: PremiumViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        viewModel = PremiumViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `analyzeJournal sukses akan mengupdate journalResult dan menyimpannya ke database lokal`() = runTest {
        // GIVEN
        val dummyCaregiverId = "caregiver_123"
        val userContent = "Hari ini saya merasa sangat senang dan produktif merawat nenek."
        
        val dummyAiResponse = Journal(
            id = "j_1",
            content = userContent,
            aiAnalysis = "Wah, ikut senang mendengarnya! Terus pertahankan semangatmu.",
            moodScore = 9,
            timestamp = 1000L,
            caregiverId = dummyCaregiverId
        )

        // Mock respon API
        coEvery { mockRepository.analyzeMood(userContent, any()) } returns dummyAiResponse

        // WHEN
        viewModel.analyzeJournal(userContent, dummyCaregiverId)
        
        // Memajukan waktu coroutine agar proses API selesai (karena delay/suspend)
        testDispatcher.scheduler.advanceUntilIdle() 

        // THEN
        // Verifikasi LiveData berubah sesuai respon
        val result = viewModel.journalResult.value
        assertEquals(9, result?.moodScore)
        assertEquals(false, viewModel.burnoutAlert.value) // Skor 9 bukan burnout
        
        // Verifikasi fungsi insertJournal ke database lokal dipanggil persis 1 kali
        coVerify(exactly = 1) { mockRepository.insertJournal(any()) }
    }
    
    @Test
    fun `analyzeJournal dengan skor 3 ke bawah akan memicu alert burnout`() = runTest {
        // GIVEN
        val dummyCaregiverId = "caregiver_456"
        val userContent = "Saya sangat lelah, kurang tidur, dan rasanya ingin menyerah."
        
        val dummyAiResponse = Journal(
            id = "j_2",
            content = userContent,
            aiAnalysis = "Kamu pasti sangat lelah. Tolong istirahatlah sejenak ya.",
            moodScore = 2, // Skor krisis / Burnout
            timestamp = 2000L,
            caregiverId = dummyCaregiverId
        )

        coEvery { mockRepository.analyzeMood(userContent, any()) } returns dummyAiResponse

        // WHEN
        viewModel.analyzeJournal(userContent, dummyCaregiverId)
        testDispatcher.scheduler.advanceUntilIdle() 

        // THEN
        val result = viewModel.journalResult.value
        assertEquals(2, result?.moodScore)
        assertEquals(true, viewModel.burnoutAlert.value) // Harus bernilai TRUE
    }

    @Test
    fun `analyzeJournal gagal (error jaringan) akan gagal tanpa memanggil DB`() = runTest {
        // GIVEN
        val dummyCaregiverId = "caregiver_789"
        val userContent = "Halo AI"
        
        // Memaksa mock untuk melempar Exception (Simulasi jaringan putus)
        coEvery { mockRepository.analyzeMood(any(), any()) } throws RuntimeException("Timeout")

        // WHEN
        viewModel.analyzeJournal(userContent, dummyCaregiverId)
        testDispatcher.scheduler.advanceUntilIdle() 

        // THEN
        // Kita pastikan bahwa insertJournal TIDAK PERNAH dipanggil karena terjadi error
        coVerify(exactly = 0) { mockRepository.insertJournal(any()) }
    }
}
