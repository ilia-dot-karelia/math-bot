package ru.tg.pawaptz.chats.math.tasks.gen

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity.EASY
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Answer

@ObsoleteCoroutinesApi
internal class CountingTaskComplexityProviderTest {

    private val dao = mockk<PostgresDao>()
    private val user = mockk<TgUser>()

    @ExperimentalCoroutinesApi
    private val channel = BroadcastChannel<UserTaskCompletion>(10)

    @ExperimentalCoroutinesApi
    private val complexityProvider = CountingTaskComplexityProvider(dao, channel.openSubscription())

    @BeforeEach
    internal fun setUp() = runBlocking {
        every { dao.getComplexityOfTaskForUser(user) }.returns(EASY)
        every { dao.updateComplexityForUser(user, any()) }.returns(Unit)
        Unit
    }

    @ExperimentalCoroutinesApi
    @AfterEach
    internal fun tearDown() = runBlocking {
        complexityProvider.stopTrackingComplexity(user)
    }

    @ExperimentalCoroutinesApi
    @ParameterizedTest
    @EnumSource(value = TaskComplexity::class, names = ["HIGH"], mode = EnumSource.Mode.EXCLUDE)
    fun whenOneHundredTasksSucceedOfOneHundredThenIncreaseTheComplexity(start: TaskComplexity) = runBlocking {
        every { dao.getComplexityOfTaskForUser(user) }.returns(start)
        complexityProvider.startTrackingComplexity(user)

        val task = SimpleMathTask(1, MathIntTaskDescription(""), answer = Answer.CorrectAnswer(20f))
        assertThat(complexityProvider.userComplexity(user)).isSameAs(start)
        repeat(100) {
            channel.send(UserTaskCompletion(ActiveUser(user, TgChatId(10)), task, Answer.CorrectAnswer(20f)))
        }

        verify(timeout = 200) { dao.updateComplexityForUser(user, start.grow()) }
    }
}