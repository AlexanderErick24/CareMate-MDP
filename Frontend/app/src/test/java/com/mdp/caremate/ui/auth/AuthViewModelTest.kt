package com.mdp.caremate.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.FamilyMember
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    // Makes LiveData work synchronously in tests (no Android main thread needed)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock the repository — we don't want real Firebase calls in unit tests
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
    fun `login success - loginState emits success with role`() = runTest {

        // Given: repository returns "family" role on success
        coEvery {
            repository.login("grace@mail.com", "password123")
        } returns Result.success("family")

        // When: login is called
        viewModel.login("grace@mail.com", "password123")

        // Then: loginState should hold a success result with "family"
        val result = viewModel.loginState.value
        assertNotNull(result)
        assertTrue(result!!.isSuccess)
        assertEquals("family", result.getOrNull())
    }

    @Test
    fun `login failed - loginState emits failure`() = runTest {

        // Given: repository returns failure
        coEvery {
            repository.login("wrong@mail.com", "wrongpass")
        } returns Result.failure(Exception("Invalid credentials"))

        // When
        viewModel.login("wrong@mail.com", "wrongpass")

        // Then: loginState should hold a failure result
        val result = viewModel.loginState.value
        assertNotNull(result)
        assertTrue(result!!.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    // ==================================================
    // REGISTER
    // ==================================================

    @Test
    fun `register family success - registerState emits success`() = runTest {

        // Given: repository returns success for family register
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
    fun `register family with invalid pairing code - registerState emits failure`() = runTest {

        // Given: repository rejects invalid pairing code
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
    fun `register family when premium limit reached - registerState emits PREMIUM_REQUIRED`() = runTest {

        // Given: the caregiver already has 2 family members (free tier limit)
        coEvery {
            repository.register("Grace", "grace@mail.com", "pass123", "family", "CM-123A", "")
        } returns Result.failure(Exception("PREMIUM_REQUIRED"))

        // When
        viewModel.register("Grace", "grace@mail.com", "pass123", "family", "CM-123A", "")

        // Then: the error message must be exactly "PREMIUM_REQUIRED" so the Fragment can handle it
        val result = viewModel.registerState.value
        assertNotNull(result)
        assertTrue(result!!.isFailure)
        assertEquals("PREMIUM_REQUIRED", result.exceptionOrNull()?.message)
    }

    // ==================================================
    // GET CURRENT USER
    // ==================================================

    @Test
    fun `getCurrentUser success - currentUser LiveData is updated`() = runTest {

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
    fun `getCurrentUser failed - currentUser LiveData stays null`() = runTest {

        // Given: user not found
        coEvery { repository.getCurrentUser() } returns Result.failure(Exception("User not found"))

        // When
        viewModel.getCurrentUser()

        // Then: currentUser was never set, so it stays null
        val user = viewModel.currentUser.value
        assertEquals(null, user)
    }

    // ==================================================
    // GET LINKED CAREGIVER
    // ==================================================

    @Test
    fun `getLinkedCaregiver success - linkedCaregiver LiveData is updated`() = runTest {

        // Given
        val fakeCaregiver = User(uid = "cg-001", name = "Dr. Ani", role = "caregiver", patientName = "Grandpa")
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
    fun `getLinkedCaregiver failed - linkedCaregiver stays null`() = runTest {

        // Given: family member has no caregiver linked
        coEvery { repository.getLinkedCaregiver() } returns Result.failure(Exception("Caregiver not linked"))

        // When
        viewModel.getLinkedCaregiver()

        // Then: linkedCaregiver is never set
        assertEquals(null, viewModel.linkedCaregiver.value)
    }

    // ==================================================
    // GET FAMILY MEMBERS
    // ==================================================

    @Test
    fun `getFamilyMembers success - familyMembers LiveData contains correct names`() = runTest {

        // Given
        coEvery { repository.getFamilyMembers() } returns Result.success(listOf("Grace", "John"))

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
    fun `getFamilyMembers returns empty list - familyMembers LiveData is empty`() = runTest {

        // Given: no family members connected yet
        coEvery { repository.getFamilyMembers() } returns Result.success(emptyList())

        // When
        viewModel.getFamilyMembers()

        // Then
        val members = viewModel.familyMembers.value
        assertNotNull(members)
        assertTrue(members!!.isEmpty())
    }
}