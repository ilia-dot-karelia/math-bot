package ru.tg.pawaptz.chats.math.tasks.cmd

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tg.api.inlined.ChatType
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.inlined.TgMessageId
import ru.tg.api.transport.TgChat
import ru.tg.api.transport.TgMessage
import ru.tg.api.transport.TgUpdate
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.gen.UserTaskManager
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Score

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class UserCommandHandlerTest {

    private val dao = mockk<PostgresDao>()
    private val usr = TgUser(1, false, FirstName("testUser"))
    private val messageDto = mockk<TgMessage>()
    private val updateDto = mockk<TgUpdate>()
    private val channel = BroadcastChannel<TgUpdate>(10)
    private val userTaskManager = mockk<UserTaskManager>()
    private val handler = UserCommandHandler(channel.openSubscription(), dao, userTaskManager, mockk(relaxed = true))
    private val tgChatId = TgChatId(10)

    @BeforeEach
    internal fun setUp() {
        every { updateDto.message } returns messageDto
        messageDto.also {
            every { it.msgId }.returns(TgMessageId(123))
            every { it.from }.returns(usr)
            every { it.chat }.returns(TgChat(10, ChatType("t"), null, null, null, null))
        }
        dao.also {
            every { it.createUserIfNotExist(any(), TgChatId(any())) } returns Unit
            every { it.setUserActive(usr) } returns Unit
            every { it.setUserInActive(usr) } returns Unit
            every { it.setUserActivityStatus(usr, any()) } returns Unit
            every { it.createUserScoresIfNotExist(usr) } returns Unit
            every { it.getUserScore(usr) } returns Score.ZERO
            every { it.initUserComplexityForUser(usr, any()) } returns Unit
        }
        userTaskManager.also {
            coEvery { it.completeUserTaskManagement(ActiveUser(usr, tgChatId)) } returns Unit
            coEvery { it.startManageUserTasks(ActiveUser(usr, tgChatId), Score(any())) } returns Unit
        }
        handler.start()
    }

    @AfterEach
    internal fun tearDown() {
        handler.stop()
    }

    @Test
    fun whenUserSubscribeThenCreateItIfNeedAndSubscribe() = runBlocking {
        every { messageDto.text } returns UserCommandHandler.Companion.subscribe
        channel.send(updateDto)

        verify(timeout = 100) { dao.createUserIfNotExist(usr, tgChatId) }
        verify { dao.setUserActive(usr) }
        coVerify { userTaskManager.startManageUserTasks(ActiveUser(usr, tgChatId), Score.ZERO) }
    }

    @Test
    fun whenUserSubscribeThenCreateScoresItIfNeedAndSubscribe() = runBlocking {
        every { messageDto.text } returns UserCommandHandler.Companion.subscribe
        channel.send(updateDto)

        verify(timeout = 100) { dao.createUserScoresIfNotExist(usr) }
        verify { dao.setUserActive(usr) }
        coVerify { userTaskManager.startManageUserTasks(ActiveUser(usr, tgChatId), Score.ZERO) }
    }

    @Test
    fun whenUserHasScoresThenUseTheScoresItIfNeedAndSubscribe() = runBlocking {
        every { messageDto.text } returns UserCommandHandler.Companion.subscribe
        every { dao.getUserScore(usr) } returns Score(100)
        channel.send(updateDto)

        verify(timeout = 100) { dao.createUserScoresIfNotExist(usr) }
        verify { dao.setUserActive(usr) }
        coVerify { userTaskManager.startManageUserTasks(ActiveUser(usr, tgChatId), Score(100)) }
    }

    @Test
    fun whenUserUnSubscribeThenCreateItIfNeedAndUnSubscribe() = runBlocking {
        every { messageDto.text } returns UserCommandHandler.Companion.unSubscribe
        channel.send(updateDto)

        verify(timeout = 100) { dao.createUserIfNotExist(usr, tgChatId) }
        verify { dao.setUserInActive(usr) }
        coVerify { userTaskManager.completeUserTaskManagement(ActiveUser(usr, tgChatId)) }
    }
}