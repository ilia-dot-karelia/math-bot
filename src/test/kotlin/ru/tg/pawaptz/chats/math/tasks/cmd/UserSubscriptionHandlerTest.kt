package ru.tg.pawaptz.chats.math.tasks.cmd

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tg.api.inlined.ChatType
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgChatDto
import ru.tg.api.transport.TgMessageDto
import ru.tg.api.transport.TgUpdateDto
import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.gen.UserTaskManager
import ru.tg.pawaptz.dao.PostgresDao

@ExperimentalCoroutinesApi
internal class UserSubscriptionHandlerTest {

    private val dao = mockk<PostgresDao>()
    private val usr = TgUserDto(1, false, FirstName("testUser"))
    private val messageDto = mockk<TgMessageDto>()
    private val updateDto = mockk<TgUpdateDto>()
    private val channel = BroadcastChannel<TgUpdateDto>(10)
    private val userTaskManager = mockk<UserTaskManager>()
    private val handler = UserSubscriptionHandler(channel.openSubscription(), dao, userTaskManager)
    private val tgChatId = TgChatId(10)

    @BeforeEach
    internal fun setUp() {
        every { updateDto.message } returns messageDto
        messageDto.also {
            every { it.msgId }.returns(123)
            every { it.from }.returns(usr)
            every { it.chat }.returns(TgChatDto(10, ChatType("t"), null, null, null, null))
        }
        dao.also {
            every { it.createUserIfNotExist(any(), TgChatId(any())) }.returns(Unit)
            every { it.setUserActive(usr) }.returns(Unit)
            every { it.setUserInActive(usr) }.returns(Unit)
            every { it.setUserActivityStatus(usr, any()) }.returns(Unit)
        }
        userTaskManager.also {
            coEvery { it.completeUserTaskManagement(ActiveUser(usr, tgChatId)) } returns Unit
            coEvery { it.startManageUserTasks(ActiveUser(usr, tgChatId)) } returns Unit
        }
        handler.start()
    }

    @AfterEach
    internal fun tearDown() {
        handler.stop()
    }

    @Test
    fun whenUserSubscribeThenCreateItIfNeedAndSubscribe() = runBlocking {
        every { messageDto.text } returns UserSubscriptionHandler.Companion.subscribe
        channel.send(updateDto)

        verify(timeout = 100) { dao.createUserIfNotExist(usr, tgChatId) }
        verify { dao.setUserActive(usr) }
        coVerify { userTaskManager.startManageUserTasks(ActiveUser(usr, tgChatId)) }
    }

    @Test
    fun whenUserUnSubscribeThenCreateItIfNeedAndUnSubscribe() = runBlocking {
        every { messageDto.text } returns UserSubscriptionHandler.Companion.unSubscribe
        channel.send(updateDto)

        verify(timeout = 100) { dao.createUserIfNotExist(usr, tgChatId) }
        verify { dao.setUserInActive(usr) }
        coVerify { userTaskManager.completeUserTaskManagement(ActiveUser(usr, tgChatId)) }
    }
}