package ru.tg.pawaptz.achievments

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.nield.kotlinstatistics.simpleRegression
import ru.tg.api.generic.TgBot
import ru.tg.api.inlined.TgChatId
import ru.tg.api.inlined.TgText
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.TaskCosts
import ru.tg.pawaptz.chats.math.tasks.gen.UserComplexityProvider
import ru.tg.pawaptz.inlined.Score
import java.util.concurrent.Executors

class AchievementSenderImpl(
    private val sendScoresAfterNTask: Int = 3,
    private val tgBot: TgBot,
    private val complexityProvider: UserComplexityProvider,
    private val taskCosts: TaskCosts,
    private val achievementMessageProvider: AchievementMessageProvider
) : AchievementSender {

    private val dsp = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val map = mutableMapOf<TgUser, MutableList<Score>>()

    override suspend fun onTaskCompleted(user: TgUser, chatId: TgChatId, currentScore: Score) {
        withContext(dsp) {
            val scores = map.computeIfAbsent(user) { mutableListOf() }
            scores.add(currentScore)
            if (scores.size.rem(sendScoresAfterNTask) == 0) {
                val regression = scores.takeLast(10).mapIndexed { i, t -> i to t.v }.asSequence().simpleRegression()
                val userComplexity = complexityProvider.userComplexity(user)
                val cost = taskCosts.getCost(userComplexity)
                val mood = when {
                    regression.slope > cost / 2 -> {
                        Mood.GOOD
                    }
                    regression.slope >= 0 -> {
                        Mood.NEUTRAL
                    }
                    else -> {
                        Mood.BAD
                    }
                }
                tgBot.sendMessage(chatId, TgText(achievementMessageProvider.get(mood, currentScore))) {
                    //TODO FIX
                }
            }
        }
    }
}