package com.mdp.caremate.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.repositories.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AuthRepository = mockk()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================================================
    // LOGIN
    // ==================================================

    @Test
    fun loginSuccess_loginStateEmitsSuccessWithRole() = runTest {

        // Given
        coEvery {
            repository.login("grace@mail.com", "password123")
        } returns Result.success("family")

        // When
        viewModel.login("grace@mail.com", "password123")

        // Then
        val result = viewModel.loginState.value
        assertNotNull(result)
        assertTrue(result!!.isSuccess)
        assertEquals("family", result.getOrNull())
    }

    @Test
    fun loginFailed_loginStateEmitsFailure() = runTest {

        // Given
        coEvery {
            repository.login("wrong@mail.com", "wrongpass")
        } returns Result.failure(Exception("Invalid credentials"))

        // When
        viewModel.login("wrong@mail.com", "wrongpass")

        // Then
        val result = viewModel.loginState.value
        assertNotNull(result)
        assertTrue(result!!.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    // ==================================================
    // REGISTER
    // ==================================================

    @Test
    fun registerFamilySuccess_registerStateEmitsSuccess() = runTest {

        // Given
        coEvery {
            repository.register("Grace", "grace@mail.com", "pass123", "family", "CM-123A", "")
        } returns Result.success("Register berhasil")

        // When
        viewModel.register("Grace", "grace@mail.com", "pass123", "family", "CM-123A", "")

        // Then
        val result = viewModel.registerState.value
        assertNotNull(result)
        assertTrue(result!!.isSuccess)
    }

    @Test
    fun registerFamilyWithInvalidPairingCode_registerStateEmitsFailure() = runTest {

        // Given
        coEvery {
            repository.register("Grace", "grace@mail.com", "pass123", "family", "INVALID", "")
        } returns Result.failure(Exception("Invalid Pairing Code"))

        // When
        viewModel.register("Grace", "grace@mail.com", "pass123", "family", "INVALID", "")

        // Then
        val result = viewModel.registerState.value
        assertNotNull(result)
        assertTrue(result!!.isFailure)
        assertEquals("Invalid Pairing Code", result.exceptionOrNull()?.message)
    }

    @Test
    fun registerFamilyWhenPremiumLimitReached_registerStateEmitsPremiumRequired() = runTest {

        // Given: caregiver already has 2 family members (free tier limit)
        coEvery {
            repository.register("Grace", "grace@mail.com", "pass123", "family", "CM-123A", "")
        } returns Result.failure(Exception("PREMIUM_REQUIRED"))

        // When
        viewModel.register("Grace", "grace@mail.com", "pass123", "family", "CM-123A", "")

        // Then
        val result = viewModel.registerState.value
        assertNotNull(result)
        assertTrue(result!!.isFailure)
        assertEquals("PREMIUM_REQUIRED", result.exceptionOrNull()?.message)
    }

    // ==================================================
    // GET CURRENT USER
    // ==================================================

    @Test
    fun getCurrentUserSuccess_currentUserLiveDataIsUpdated() = runTest {

        // Given
        val fakeUser = User(uid = "uid-001", name = "Grace", email = "grace@mail.com", role = "family")
        coEvery { repository.getCurrentUser() } returns Result.success(fakeUser)

        // When
        viewModel.getCurrentUser()

        // Then
        val user = viewModel.currentUser.value
        assertNotNull(user)
        assertEquals("Grace", user!!.name)
        assertEquals("family", user.role)
    }

    @Test
    fun getCurrentUserFailed_currentUserLiveDataStaysNull() = runTest {

        // Given
        coEvery { repository.getCurrentUser() } returns Result.failure(Exception("User not found"))

        // When
        viewModel.getCurrentUser()

        // Then
        assertNull(viewModel.currentUser.value)
    }

    // ==================================================
    // GET LINKED CAREGIVER
    // ==================================================

    @Test
    fun getLinkedCaregiverSuccess_linkedCaregiverLiveDataIsUpdated() = runTest {

        // Given
        val fakeCaregiver = User(
            uid = "cg-001",
            name = "Dr. Ani",
            role = "caregiver",
            patientName = "Grandpa"
        )
        coEvery { repository.getLinkedCaregiver() } returns Result.success(fakeCaregiver)

        // When
        viewModel.getLinkedCaregiver()

        // Then
        val caregiver = viewModel.linkedCaregiver.value
        assertNotNull(caregiver)
        assertEquals("Dr. Ani", caregiver!!.name)
        assertEquals("Grandpa", caregiver.patientName)
    }

    @Test
    fun getLinkedCaregiverFailed_linkedCaregiverStaysNull() = runTest {

        // Given
        coEvery {
            repository.getLinkedCaregiver()
        } returns Result.failure(Exception("Caregiver not linked"))

        // When
        viewModel.getLinkedCaregiver()

        // Then
        assertNull(viewModel.linkedCaregiver.value)
    }

    // ==================================================
    // GET FAMILY MEMBERS
    // ==================================================

    @Test
    fun getFamilyMembersSuccess_familyMembersLiveDataContainsCorrectNames() = runTest {

        // Given
        coEvery {
            repository.getFamilyMembers()
        } returns Result.success(listOf("Grace", "John"))

        // When
        viewModel.getFamilyMembers()

        // Then
        val members = viewModel.familyMembers.value
        assertNotNull(members)
        assertEquals(2, members!!.size)
        assertTrue(members.contains("Grace"))
        assertTrue(members.contains("John"))
    }

    @Test
    fun getFamilyMembersReturnsEmptyList_familyMembersLiveDataIsEmpty() = runTest {

        // Given
        coEvery {
            repository.getFamilyMembers()
        } returns Result.success(emptyList())

        // When
        viewModel.getFamilyMembers()

        // Then
        val members = viewModel.familyMembers.value
        assertNotNull(members)
        assertTrue(members!!.isEmpty())
    }
}