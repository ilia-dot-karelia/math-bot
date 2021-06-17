package ru.tg.pawaptz.chats.math

import anyChat
import anyUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tg.api.generic.TgBot
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.inlined.TgMessageId
import ru.tg.api.inlined.TgText
import ru.tg.api.transport.TgMessage
import ru.tg.api.transport.TgUpdate
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity.EASY
import ru.tg.pawaptz.inlined.Answer
import kotlin.time.ExperimentalTime

@ObsoleteCoroutinesApi
@ExperimentalTime
@ExperimentalCoroutinesApi
internal class TgTaskUpdaterImplTest {

    private val bot = mockk<TgBot>()
    private val channel = BroadcastChannel<TgUpdate>(10)
    private val updater = TgTaskUpdaterImpl(bot)

    @BeforeEach
    internal fun setUp() {
        bot.also {
            coEvery { it.sendMessage(TgChatId(any()), TgText(any()), any()) } answers {
                TgMessage(
                    TgMessageId(1), anyUser, anyChat, 12, anyChat, "text", null, null
                )
            }
            every { it.subscribe() } returns channel.openSubscription()
        }
    }

    @Test
    fun whenNewTaskThenSendItToTheUsersChat() = runBlocking {
        val chatId = TgChatId(100)
        updater.update(ActiveUser(TgUser(123, false, FirstName("test")), chatId), genMathTask())

        coVerify(timeout = 300) {
            bot.sendMessage(chatId, TgText(any()), any())
        }
    }

    @Test
    fun test2() {

    }

    private fun genMathTask(): MathTask {
        return SimpleMathTask(
            15, MathIntTaskDescription("2+2"), EASY, Answer.CorrectAnswer(4f), true
        )
    }
}