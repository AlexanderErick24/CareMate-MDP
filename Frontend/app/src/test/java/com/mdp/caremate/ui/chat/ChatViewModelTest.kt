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

    // A fake ListenerRegistration that does nothing when removed
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
    fun `loadChatRoom success - pairingCode LiveData is set`() = runTest {

        // Given: getPairingCodeForCurrentUser returns a code
        // and observeMessages + observeChatRoom return fake listeners
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
    fun `loadChatRoom failed - pairingCode LiveData stays null`() = runTest {

        // Given: no pairing code found (user not linked)
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.failure(Exception("User not found"))

        // When
        viewModel.loadChatRoom()

        // Then: onSuccess is never called, pairingCode stays null
        assertNull(viewModel.pairingCode.value)
    }

    // ==================================================
    // OBSERVE MESSAGES
    // ==================================================

    @Test
    fun `loadChatRoom - messages LiveData receives messages from listener`() = runTest {

        // Given: fake messages that the listener will "push"
        val fakeMessages = listOf(
            ChatMessage(senderId = "uid-001", senderName = "Grace", message = "Hello!", timestamp = 1000L),
            ChatMessage(senderId = "uid-002", senderName = "Dr. Ani", message = "Hi Grace!", timestamp = 2000L)
        )

        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        // Simulate the listener immediately calling onMessagesChanged with fakeMessages
        every {
            repository.observeMessages("CM-456B", any())
        } answers {
            // The second argument is the callback; invoke it right away
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
    fun `loadChatRoom - empty messages list is handled correctly`() = runTest {

        // Given: no messages in the chat yet
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

        // Then: messages is an empty list, not null — adapter should show empty state
        val messages = viewModel.messages.value
        assertNotNull(messages)
        assertEquals(0, messages!!.size)
    }

    // ==================================================
    // SEND MESSAGE
    // ==================================================

    @Test
    fun `sendMessage - repository sendMessage is called when pairingCode is set`() = runTest {

        // Given: pairingCode is already loaded
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

        // Then: verify the repository was actually called
        coVerify { repository.sendMessage("CM-456B", "Hello!") }
    }

    @Test
    fun `sendMessage - repository is NOT called when pairingCode is null`() = runTest {

        // Given: loadChatRoom was never called, so pairingCode is null

        // When: sendMessage is called without a pairing code
        viewModel.sendMessage("Hello!")

        // Then: repository.sendMessage should never be called
        coVerify(exactly = 0) { repository.sendMessage(any(), any()) }
    }

    // ==================================================
    // MARK AS READ
    // ==================================================

    @Test
    fun `markAsRead - calls markChatAsRead when pairingCode is already set`() = runTest {

        // Given: pairingCode is already set (after loadChatRoom)
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
    fun `markAsRead - loads pairingCode first if not set yet then calls markChatAsRead`() = runTest {

        // Given: pairingCode is null (markAsRead called before loadChatRoom)
        coEvery {
            repository.getPairingCodeForCurrentUser()
        } returns Result.success("CM-456B")

        coEvery {
            repository.markChatAsRead("CM-456B")
        } returns Result.success(Unit)

        // When: markAsRead is called before loadChatRoom
        viewModel.markAsRead()

        // Then: it should have fetched the pairing code and marked as read
        coVerify { repository.markChatAsRead("CM-456B") }
    }

    // ==================================================
    // UNREAD BADGE (from observeChatRoom)
    // ==================================================

    @Test
    fun `loadChatRoom - unreadCount LiveData is updated from room observer`() = runTest {

        // Given: the room document shows 3 unread messages
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
    fun `loadChatRoom - unreadCount is 0 when all messages are read`() = runTest {

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