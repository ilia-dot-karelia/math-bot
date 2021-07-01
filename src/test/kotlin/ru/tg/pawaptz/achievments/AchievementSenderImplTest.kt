package ru.tg.pawaptz.achievments

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.tg.api.generic.TgBot
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.inlined.TgText
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.TaskCosts
import ru.tg.pawaptz.chats.math.tasks.gen.UserComplexityProvider
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity.*
import ru.tg.pawaptz.inlined.Score

internal class AchievementSenderImplTest {

    private val taskCosts = TaskCosts(
        mapOf(EASY to 10, MIDDLE to 15, ADVANCED to 20, HARD to 30)
    )
    private val achievementMessageProvider = mockk<AchievementMessageProvider>()
    private val bot = mockk<TgBot>(relaxed = true)
    private val complexityProvider = mockk<UserComplexityProvider>()
    private val achievementSender =
        AchievementSenderImpl(3, bot, complexityProvider, taskCosts, achievementMessageProvider)
    private val chatId = TgChatId(1)
    private val user = TgUser(1, false, FirstName("test-user"))

    @BeforeEach
    internal fun setUp() {
        coEvery { complexityProvider.userComplexity(user) } returns EASY
        coEvery { achievementMessageProvider.get(any(), Score(any())) } returns ""
    }

    @Test
    fun whenCompletionThenSendScoresEveryNConfiguredTimes() = runBlocking {
        achievementSender.onTaskCompleted(user, chatId, Score.ZERO)
        achievementSender.onTaskCompleted(user, chatId, Score.ZERO)
        achievementSender.onTaskCompleted(user, chatId, Score.ZERO)
        achievementSender.onTaskCompleted(user, chatId, Score.ZERO)

        coVerify { bot.sendMessage(chatId, TgText(any()), any()) }
    }

    @Test
    fun whenCompletionLessThanNConfiguredTimesThenDoNotSendUpdateScores() = runBlocking {
        achievementSender.onTaskCompleted(user, chatId, Score.ZERO)
        achievementSender.onTaskCompleted(user, chatId, Score.ZERO)

        coVerify(exactly = 0) { bot.sendMessage(chatId, TgText(any()), any()) }
    }

    @ParameterizedTest
    @EnumSource(TaskComplexity::class)
    fun whenSlopeIsMaximumThenSendMessageOfGoodMood(complexity: TaskComplexity) = runBlocking {
        every { achievementMessageProvider.get(Mood.GOOD, Score(any())) } returns "well done"
        val easyCost = Score(taskCosts.getCost(complexity))

        achievementSender.onTaskCompleted(user, chatId, easyCost * 1)
        achievementSender.onTaskCompleted(user, chatId, easyCost * 2)
        achievementSender.onTaskCompleted(user, chatId, easyCost * 3)

        coVerify { bot.sendMessage(chatId, TgText("well done"), any()) }
    }

    @ParameterizedTest
    @EnumSource(TaskComplexity::class)
    fun whenSlopeIsMinimumThenSendMessageOfBadMood(complexity: TaskComplexity) = runBlocking {
        coEvery { complexityProvider.userComplexity(user) } returns complexity
        every { achievementMessageProvider.get(Mood.BAD, Score(any())) } returns "not good"
        val easyCost = Score(taskCosts.getCost(complexity))

        achievementSender.onTaskCompleted(user, chatId, easyCost * 3)
        achievementSender.onTaskCompleted(user, chatId, easyCost * 2)
        achievementSender.onTaskCompleted(user, chatId, easyCost * 1)

        coVerify { bot.sendMessage(chatId, TgText("not good"), any()) }
    }

    @ParameterizedTest
    @EnumSource(TaskComplexity::class)
    fun whenSlopeIsNeutralThenSendMessageOfNeutralMood(complexity: TaskComplexity) = runBlocking {
        coEvery { complexityProvider.userComplexity(user) } returns complexity
        every { achievementMessageProvider.get(Mood.NEUTRAL, Score(any())) } returns "next"
        val easyCost = Score(taskCosts.getCost(complexity))

        achievementSender.onTaskCompleted(user, chatId, easyCost)
        achievementSender.onTaskCompleted(user, chatId, easyCost)
        achievementSender.onTaskCompleted(user, chatId, easyCost)

        coVerify { bot.sendMessage(chatId, TgText("next"), any()) }
    }

}