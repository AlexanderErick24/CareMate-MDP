package com.mdp.caremate.ui.family.management

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.FamilyMember
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
class FamilyManagementViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AuthRepository = mockk()

    private lateinit var viewModel: FamilyManagementViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FamilyManagementViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================================================
    // LOAD FAMILY MEMBERS
    // ==================================================

    @Test
    fun `loadFamilyMembers success - familyMembers LiveData shows all members`() = runTest {

        // Given: two family members exist
        val fakeMembers = listOf(
            FamilyMember(uid = "f-001", name = "Grace", email = "grace@mail.com"),
            FamilyMember(uid = "f-002", name = "John", email = "john@mail.com")
        )
        coEvery { repository.getFamilyMemberList() } returns Result.success(fakeMembers)

        // When
        viewModel.loadFamilyMembers()

        // Then
        val members = viewModel.familyMembers.value
        assertNotNull(members)
        assertEquals(2, members!!.size)
        assertEquals("Grace", members[0].name)
        assertEquals("John", members[1].name)
    }

    @Test
    fun `loadFamilyMembers with single member - familyMembers LiveData has one item`() = runTest {

        // Given
        val fakeMembers = listOf(
            FamilyMember(uid = "f-001", name = "Grace", email = "grace@mail.com")
        )
        coEvery { repository.getFamilyMemberList() } returns Result.success(fakeMembers)

        // When
        viewModel.loadFamilyMembers()

        // Then
        val members = viewModel.familyMembers.value
        assertNotNull(members)
        assertEquals(1, members!!.size)
    }

    @Test
    fun `loadFamilyMembers with empty result - familyMembers LiveData is empty`() = runTest {

        // Given: no family members yet
        coEvery { repository.getFamilyMemberList() } returns Result.success(emptyList())

        // When
        viewModel.loadFamilyMembers()

        // Then
        val members = viewModel.familyMembers.value
        assertNotNull(members)
        assertTrue(members!!.isEmpty())
    }

    @Test
    fun `loadFamilyMembers failed - familyMembers LiveData stays null`() = runTest {

        // Given: network/Firestore error
        coEvery { repository.getFamilyMemberList() } returns Result.failure(Exception("Network error"))

        // When
        viewModel.loadFamilyMembers()

        // Then: onSuccess is never called, so LiveData is never set
        assertEquals(null, viewModel.familyMembers.value)
    }

    // ==================================================
    // PREMIUM LIMIT LOGIC
    // The premium limit (max 2 family members for free tier)
    // is enforced in FirebaseSource.registerFamily().
    // The ViewModel exposes _isPremiumRequired for the Fragment to show a warning.
    // We test that it starts as null (not triggered unless explicitly set).
    // ==================================================

    @Test
    fun `isPremiumRequired is null by default - no false alarm on startup`() {

        // The ViewModel was just created in setUp().
        // isPremiumRequired should never be true unless explicitly triggered.
        val isPremium = viewModel.isPremiumRequired.value
        assertEquals(null, isPremium)
    }

    @Test
    fun `loadFamilyMembers with 2 members - familyMembers shows free tier is full`() = runTest {

        // Given: exactly 2 members (free tier max)
        val fakeMembers = listOf(
            FamilyMember(uid = "f-001", name = "Grace", email = "grace@mail.com"),
            FamilyMember(uid = "f-002", name = "John", email = "john@mail.com")
        )
        coEvery { repository.getFamilyMemberList() } returns Result.success(fakeMembers)

        // When
        viewModel.loadFamilyMembers()

        // Then: 2 members loaded correctly — attempting to add a 3rd would trigger PREMIUM_REQUIRED
        // (that check is done in FirebaseSource, not here, but we confirm count is correct)
        assertEquals(2, viewModel.familyMembers.value!!.size)
    }
}