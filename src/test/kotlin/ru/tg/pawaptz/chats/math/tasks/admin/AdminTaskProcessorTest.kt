package ru.tg.pawaptz.chats.math.tasks.admin

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.dao.PostgresDao

internal class AdminTaskProcessorTest {

    private val channel = mockk<AdminMathTaskChannel>()
    private val pgDao = mockk<PostgresDao>()
    private val adminUser = TgUser(1, false, FirstName("adm"))
    private val adminChatId = TgChatId(123)
    private val tp = AdminTaskProcessor(channel, pgDao, adminUser, adminChatId)
    private val testChannel = Channel<MathTask>()

    @ExperimentalCoroutinesApi
    @BeforeEach
    internal fun setUp() {
        every { channel.subscribe() }.returns(testChannel)
        every { pgDao.saveTask(any()) }.returns(1234)
        every { pgDao.createUserScoresIfNotExist(adminUser) } returns Unit
        every { pgDao.createUserIfNotExist(adminUser, adminChatId) } returns Unit
        tp.start()
    }

    @Test
    fun whenAdminTaskReceivedThenSaveToDb() {
        val tsk = mockk<MathTask>()
        testChannel.trySend(tsk)
        verify(timeout = 200) { pgDao.saveTask(tsk) }
    }
}