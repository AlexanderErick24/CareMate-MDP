package com.mdp.caremate.ui.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.repositories.ProfileRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: ProfileRepository = mockk()

    private lateinit var viewModel: ProfileViewModel

    private val fakeUser = User(
        uid = "uid-001",
        name = "Grace",
        email = "grace@mail.com",
        role = "family",
        photoUrl = ""
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================================================
    // FETCH CURRENT USER
    // ==================================================

    @Test
    fun fetchCurrentUserSuccess_userStateLiveDataContainsUserData() = runTest {

        // Given
        coEvery { repository.getCurrentUser() } returns Result.success(fakeUser)

        // When
        viewModel.fetchCurrentUser()

        // Then
        val user = viewModel.userState.value
        assertNotNull(user)
        assertEquals("Grace", user!!.name)
        assertEquals("grace@mail.com", user.email)
        assertEquals("family", user.role)
    }

    @Test
    fun fetchCurrentUserSuccess_isLoadingIsFalseAfterCompletion() = runTest {

        // Given
        coEvery { repository.getCurrentUser() } returns Result.success(fakeUser)

        // When
        viewModel.fetchCurrentUser()

        // Then
        assertFalse(viewModel.isLoading.value!!)
    }

    @Test
    fun fetchCurrentUserFailed_userStateStaysNullAndToastMessageIsSet() = runTest {

        // Given
        coEvery {
            repository.getCurrentUser()
        } returns Result.failure(Exception("User not found"))

        // When
        viewModel.fetchCurrentUser()

        // Then
        assertNull(viewModel.userState.value)
        val msg = viewModel.toastMessage.value
        assertNotNull(msg)
        assertTrue(msg!!.contains("Gagal"))
    }

    // ==================================================
    // UPDATE USERNAME
    // ==================================================

    @Test
    fun updateUsernameSuccess_toastMessageShowsSuccessAndUserIsRefreshed() = runTest {

        // Given
        val updatedUser = fakeUser.copy(name = "Grace Updated")
        coEvery {
            repository.updateUsername("Grace Updated")
        } returns Result.success("Nama berhasil diperbarui")
        coEvery { repository.getCurrentUser() } returns Result.success(updatedUser)

        // When
        viewModel.updateUsername("Grace Updated")

        // Then
        val msg = viewModel.toastMessage.value
        assertNotNull(msg)
        assertEquals("Nama berhasil diperbarui!", msg)
        assertEquals("Grace Updated", viewModel.userState.value?.name)
    }

    @Test
    fun updateUsernameFailed_toastMessageShowsError() = runTest {

        // Given
        coEvery {
            repository.updateUsername("Grace Updated")
        } returns Result.failure(Exception("Network error"))

        // When
        viewModel.updateUsername("Grace Updated")

        // Then
        val msg = viewModel.toastMessage.value
        assertNotNull(msg)
        assertTrue(msg!!.contains("Gagal"))
    }

    // ==================================================
    // CHANGE PASSWORD
    // ==================================================

    @Test
    fun changePasswordSuccess_toastMessageShowsSuccess() = runTest {

        // Given
        coEvery {
            repository.changePassword("oldPass123", "newPass456")
        } returns Result.success("Password berhasil diubah")

        // When
        viewModel.changePassword("oldPass123", "newPass456")

        // Then
        val msg = viewModel.toastMessage.value
        assertNotNull(msg)
        assertEquals("Password berhasil diubah!", msg)
    }

    @Test
    fun changePasswordFailed_toastMessageShowsError() = runTest {

        // Given
        coEvery {
            repository.changePassword("wrongPass", "newPass456")
        } returns Result.failure(Exception("Wrong current password"))

        // When
        viewModel.changePassword("wrongPass", "newPass456")

        // Then
        val msg = viewModel.toastMessage.value
        assertNotNull(msg)
        assertTrue(msg!!.contains("Gagal"))
    }

    @Test
    fun changePasswordFailed_isLoadingIsFalseAfterFailure() = runTest {

        // Given
        coEvery {
            repository.changePassword(any(), any())
        } returns Result.failure(Exception("Error"))

        // When
        viewModel.changePassword("any", "any")

        // Then: loading is always reset after the operation completes
        assertFalse(viewModel.isLoading.value!!)
    }

    // ==================================================
    // UPLOAD PROFILE PHOTO
    // ==================================================

    @Test
    fun uploadProfilePhotoSuccess_toastMessageShowsSuccessAndUserIsRefreshed() = runTest {

        // Given
        val fakeBase64 = "base64encodedstring"
        val updatedUser = fakeUser.copy(photoUrl = fakeBase64)
        coEvery {
            repository.uploadProfilePhoto(fakeBase64)
        } returns Result.success(fakeBase64)
        coEvery { repository.getCurrentUser() } returns Result.success(updatedUser)

        // When
        viewModel.uploadProfilePhoto(fakeBase64)

        // Then
        val msg = viewModel.toastMessage.value
        assertNotNull(msg)
        assertEquals("Foto profil berhasil diperbarui!", msg)
        assertEquals(fakeBase64, viewModel.userState.value?.photoUrl)
    }

    @Test
    fun uploadProfilePhotoFailed_toastMessageShowsError() = runTest {

        // Given
        coEvery {
            repository.uploadProfilePhoto(any())
        } returns Result.failure(Exception("Upload failed"))

        // When
        viewModel.uploadProfilePhoto("some_base64")

        // Then
        val msg = viewModel.toastMessage.value
        assertNotNull(msg)
        assertTrue(msg!!.contains("Gagal"))
    }
}