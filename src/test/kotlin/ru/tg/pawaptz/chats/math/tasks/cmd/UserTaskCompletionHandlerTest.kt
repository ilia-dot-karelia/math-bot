package ru.tg.pawaptz.chats.math.tasks.cmd

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Answer

@ExperimentalCoroutinesApi
internal class UserTaskCompletionHandlerTest {

    private val dao = mockk<PostgresDao>()
    private val usr = TgUserDto(1, false, FirstName("testUser"))

    private val channel = BroadcastChannel<UserTaskCompletion>(10)

    private val listener = UserTaskCompletionHandler(dao, channel.openSubscription())

    @BeforeEach
    internal fun setUp() {
        dao.also {
            every { it.saveTask(any()) }.returns(1234)
            every { it.saveAnswer(any(), any(), Answer(any()), any()) }.returns(Unit)
        }
        listener.listen()
    }

    @AfterEach
    internal fun tearDown() {
        listener.stop()
    }

    @Test
    fun whenCorrectAnswerReceivedThenItMustBeSavedAsCorrect() = runBlocking {
        val userAnswer = Answer(3f)
        val task = SimpleMathTask(16, MathIntTaskDescription("1 + 2"), answer = Answer(3f))
        channel.send(UserTaskCompletion(ActiveUser(usr, TgChatId(10)), task, userAnswer))

        verify(timeout = 200) { dao.saveAnswer(task.id(), usr.id, userAnswer, true) }
    }

    @Test
    fun whenInCorrectAnswerReceivedThenItMustBeSavedAsCorrect() = runBlocking {
        val userAnswer = Answer(4f)
        val task = SimpleMathTask(16, MathIntTaskDescription("1 + 2"), answer = Answer(3f))
        channel.send(UserTaskCompletion(ActiveUser(usr, TgChatId(10)), task, userAnswer))

        verify(timeout = 200) { dao.saveAnswer(task.id(), usr.id, userAnswer, false) }
    }
}