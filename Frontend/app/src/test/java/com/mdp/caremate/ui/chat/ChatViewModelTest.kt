package com.mdp.caremate.ui.chat

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.firestore.ListenerRegistration
import com.mdp.caremate.data.model.ChatMessage
import com.mdp.caremate.data.model.ChatRoom
import com.mdp.caremate.data.repositories.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: ChatRepository = mockk()

    private val fakeListener: ListenerRegistration = mockk(relaxed = true)

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================================================
    // LOAD CHAT ROOM
    // ==================================================

    @Test
    fun loadChatRoomSuccess_pairingCodeLiveDataIsSet() = runTest {

        // Given
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        every {
            repository.observeMessages("CM-456B", any())
        } returns fakeListener

        every {
            repository.observeChatRoom("CM-456B", any())
        } returns fakeListener

        // When
        viewModel.loadChatRoom()

        // Then
        assertEquals("CM-456B", viewModel.pairingCode.value)
    }

    @Test
    fun loadChatRoomFailed_pairingCodeLiveDataStaysNull() = runTest {

        // Given
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.failure(Exception("User not found"))

        // When
        viewModel.loadChatRoom()

        // Then
        assertNull(viewModel.pairingCode.value)
    }

    // ==================================================
    // OBSERVE MESSAGES
    // ==================================================

    @Test
    fun loadChatRoom_messagesLiveDataReceivesMessagesFromListener() = runTest {

        // Given
        val fakeMessages = listOf(
            ChatMessage(senderId = "uid-001", senderName = "Grace", message = "Hello!", timestamp = 1000L),
            ChatMessage(senderId = "uid-002", senderName = "Dr. Ani", message = "Hi Grace!", timestamp = 2000L)
        )

        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        every {
            repository.observeMessages("CM-456B", any())
        } answers {
            val callback = secondArg<(List<ChatMessage>) -> Unit>()
            callback(fakeMessages)
            fakeListener
        }

        every {
            repository.observeChatRoom("CM-456B", any())
        } returns fakeListener

        // When
        viewModel.loadChatRoom()

        // Then
        val messages = viewModel.messages.value
        assertNotNull(messages)
        assertEquals(2, messages!!.size)
        assertEquals("Hello!", messages[0].message)
        assertEquals("Hi Grace!", messages[1].message)
    }

    @Test
    fun loadChatRoom_emptyMessagesListIsHandledCorrectly() = runTest {

        // Given
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        every {
            repository.observeMessages("CM-456B", any())
        } answers {
            val callback = secondArg<(List<ChatMessage>) -> Unit>()
            callback(emptyList())
            fakeListener
        }

        every {
            repository.observeChatRoom("CM-456B", any())
        } returns fakeListener

        // When
        viewModel.loadChatRoom()

        // Then
        val messages = viewModel.messages.value
        assertNotNull(messages)
        assertEquals(0, messages!!.size)
    }

    // ==================================================
    // SEND MESSAGE
    // ==================================================

    @Test
    fun sendMessage_repositorySendMessageIsCalledWhenPairingCodeIsSet() = runTest {

        // Given
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        every {
            repository.observeMessages("CM-456B", any())
        } returns fakeListener

        every {
            repository.observeChatRoom("CM-456B", any())
        } returns fakeListener

        coEvery {
            repository.sendMessage("CM-456B", "Hello!")
        } returns Result.success(Unit)

        viewModel.loadChatRoom()

        // When
        viewModel.sendMessage("Hello!")

        // Then
        coVerify { repository.sendMessage("CM-456B", "Hello!") }
    }

    @Test
    fun sendMessage_repositoryIsNotCalledWhenPairingCodeIsNull() = runTest {

        // Given: loadChatRoom was never called, pairingCode is null

        // When
        viewModel.sendMessage("Hello!")

        // Then
        coVerify(exactly = 0) { repository.sendMessage(any(), any()) }
    }

    // ==================================================
    // MARK AS READ
    // ==================================================

    @Test
    fun markAsRead_callsMarkChatAsReadWhenPairingCodeIsAlreadySet() = runTest {

        // Given
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        every {
            repository.observeMessages("CM-456B", any())
        } returns fakeListener

        every {
            repository.observeChatRoom("CM-456B", any())
        } returns fakeListener

        coEvery {
            repository.markChatAsRead("CM-456B")
        } returns Result.success(Unit)

        viewModel.loadChatRoom()

        // When
        viewModel.markAsRead()

        // Then
        coVerify { repository.markChatAsRead("CM-456B") }
    }

    @Test
    fun markAsRead_loadsPairingCodeFirstIfNotSetThenCallsMarkChatAsRead() = runTest {

        // Given: pairingCode is null (markAsRead called before loadChatRoom)
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        coEvery {
            repository.markChatAsRead("CM-456B")
        } returns Result.success(Unit)

        // When: markAsRead is called before loadChatRoom
        viewModel.markAsRead()

        // Then
        coVerify { repository.markChatAsRead("CM-456B") }
    }

    // ==================================================
    // UNREAD BADGE (from observeChatRoom)
    // ==================================================

    @Test
    fun loadChatRoom_unreadCountLiveDataIsUpdatedFromRoomObserver() = runTest {

        // Given: room document shows 3 unread messages
        val fakeRoom = ChatRoom(
            pairingCode = "CM-456B",
            lastSenderId = "uid-002",
            unreadCount = 3
        )

        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        every {
            repository.observeMessages("CM-456B", any())
        } returns fakeListener

        every {
            repository.observeChatRoom("CM-456B", any())
        } answers {
            val callback = secondArg<(ChatRoom) -> Unit>()
            callback(fakeRoom)
            fakeListener
        }

        // When
        viewModel.loadChatRoom()

        // Then
        assertEquals(3, viewModel.unreadCount.value)
        assertEquals("uid-002", viewModel.lastSenderId.value)
    }

    @Test
    fun loadChatRoom_unreadCountIsZeroWhenAllMessagesAreRead() = runTest {

        // Given: no unread messages
        val fakeRoom = ChatRoom(
            pairingCode = "CM-456B",
            lastSenderId = "uid-001",
            unreadCount = 0
        )

        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        every {
            repository.observeMessages("CM-456B", any())
        } returns fakeListener

        every {
            repository.observeChatRoom("CM-456B", any())
        } answers {
            val callback = secondArg<(ChatRoom) -> Unit>()
            callback(fakeRoom)
            fakeListener
        }

        // When
        viewModel.loadChatRoom()

        // Then: no badge should appear
        assertEquals(0, viewModel.unreadCount.value)
    }
}