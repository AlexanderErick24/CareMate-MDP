package com.mdp.caremate

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.repositories.ProfileRepository
import com.mdp.caremate.ui.profile.ProfileViewModel
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
class ProfileValidationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: ProfileRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateUserProfile_emptyName_returnsError() {
        val viewModel = ProfileViewModel(mockRepository)
        viewModel.updateUserProfile(
            name = "   ",
            jobTitle = "Caregiver",
            age = 30,
            bio = "Berpengalaman 5 tahun",
            experience = listOf("RS A"),
            skills = listOf("CPR")
        )
        assertEquals("Nama tidak boleh kosong", viewModel.toastMessage.value)
    }

    @Test
    fun updateUserProfile_negativeAge_returnsError() {
        val viewModel = ProfileViewModel(mockRepository)
        viewModel.updateUserProfile(
            name = "Rina Caregiver",
            jobTitle = "Caregiver",
            age = -5,
            bio = "Berpengalaman 5 tahun",
            experience = listOf("RS A"),
            skills = listOf("CPR")
        )
        assertEquals("Usia harus bernilai valid (0 - 120 tahun)", viewModel.toastMessage.value)
    }

    @Test
    fun updateUserProfile_tooOldAge_returnsError() {
        val viewModel = ProfileViewModel(mockRepository)
        viewModel.updateUserProfile(
            name = "Rina Caregiver",
            jobTitle = "Caregiver",
            age = 150,
            bio = "Berpengalaman 5 tahun",
            experience = listOf("RS A"),
            skills = listOf("CPR")
        )
        assertEquals("Usia harus bernilai valid (0 - 120 tahun)", viewModel.toastMessage.value)
    }

    @Test
    fun updateUserProfile_excessiveBioLength_returnsError() {
        val viewModel = ProfileViewModel(mockRepository)
        val longBio = "A".repeat(505)
        viewModel.updateUserProfile(
            name = "Rina Caregiver",
            jobTitle = "Caregiver",
            age = 30,
            bio = longBio,
            experience = listOf("RS A"),
            skills = listOf("CPR")
        )
        assertEquals("Bio maksimal 500 karakter", viewModel.toastMessage.value)
    }

    @Test
    fun updateUserProfile_validInput_callsRepositorySuccess() = runTest {
        coEvery {
            mockRepository.updateUserProfile(any(), any(), any(), any(), any(), any())
        } returns Result.success("Sukses")

        val viewModel = ProfileViewModel(mockRepository)
        viewModel.updateUserProfile(
            name = "Rina Caregiver",
            jobTitle = "Perawat Senior",
            age = 30,
            bio = "Siap mendampingi pasien dengan penuh kasih.",
            experience = listOf("RS A", "Klinik B"),
            skills = listOf("Pertolongan Pertama", "Terapi Lansia")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Profil berhasil diperbarui!", viewModel.toastMessage.value)
    }
}
