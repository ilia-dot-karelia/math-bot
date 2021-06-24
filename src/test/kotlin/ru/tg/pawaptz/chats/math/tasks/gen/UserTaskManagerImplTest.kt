package ru.tg.pawaptz.chats.math.tasks.gen

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.TgTaskUpdater
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.TaskCosts
import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Answer

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal class UserTaskManagerImplTest {
    private val dao = mockk<PostgresDao>()
    private val userComplexityProvider = mockk<UserComplexityProvider>()
    private val channel = BroadcastChannel<UserTaskCompletion>(10)
    private val taskGenStrategy = mockk<TaskGenStrategy>()
    private val tgTaskUpdater = mockk<TgTaskUpdater>().also {
        every { it.subscribe() } returns channel.openSubscription()
    }
    private val usr = TgUser(1, false, FirstName("testUser"))
    private val taskCosts = TaskCosts(mapOf(TaskComplexity.EASY to 10))
    private val userTskManager =
        UserTaskManagerImpl(
            userComplexityProvider,
            taskGenStrategy,
            tgTaskUpdater,
            dao,
            channel.openSubscription(),
            taskCosts
        )

    @BeforeEach
    internal fun setUp() = runBlocking {
        dao.also {
            every { it.saveTask(any()) }.returns(1234)
            every { it.saveAnswer(any(), any(), any()) }.returns(Unit)
            every { it.getAllActiveUsers() } returns listOf()
        }
        userComplexityProvider.also {
            coEvery { it.appropriateComplexity(usr) } returns TaskComplexity.EASY
            coEvery { it.startTrackingComplexity(usr) } returns Unit
            coEvery { it.stopTrackingComplexity(usr) } returns Unit
        }
        userTskManager.start()
        /*userTskManager.startManageUserTasks(ActiveUser(usr, TgChatId(123)))*/
    }

    @AfterEach
    internal fun tearDown() {
        userTskManager.stop()
    }

    @Test
    fun whenCorrectAnswerReceivedThenItMustBeSavedAsCorrect() = runBlocking {
        val userAnswer = Answer.CorrectAnswer(3f)
        val task = SimpleMathTask(16, MathIntTaskDescription("1 + 2"), answer = Answer.CorrectAnswer(3f))
        channel.send(UserTaskCompletion(ActiveUser(usr, TgChatId(10)), task, userAnswer))

        verify(timeout = 200) { dao.saveAnswer(task.id(), usr.id, userAnswer) }
    }

    @Test
    fun whenNoAnswerReceivedThenItMustNotBeSaved() = runBlocking {
        val userAnswer = Answer.NoAnswer
        val task = SimpleMathTask(16, MathIntTaskDescription("1 + 2"), answer = Answer.CorrectAnswer(3f))
        channel.send(UserTaskCompletion(ActiveUser(usr, TgChatId(10)), task, userAnswer))

        verify(timeout = 200, exactly = 0) { dao.saveAnswer(task.id(), usr.id, userAnswer) }
    }

    @Test
    fun whenInCorrectAnswerReceivedThenItMustBeSavedAsCorrect() = runBlocking {
        val userAnswer = Answer.InCorrectAnswer(4f)
        val task = SimpleMathTask(16, MathIntTaskDescription("1 + 2"), answer = Answer.CorrectAnswer(3f))
        channel.send(UserTaskCompletion(ActiveUser(usr, TgChatId(10)), task, userAnswer))

        verify(timeout = 200) { dao.saveAnswer(task.id(), usr.id, userAnswer) }
    }
}