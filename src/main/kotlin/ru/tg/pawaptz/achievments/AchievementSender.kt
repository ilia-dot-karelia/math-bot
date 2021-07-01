package ru.tg.pawaptz.achievments

import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.inlined.Score

interface AchievementSender {

    suspend fun onTaskCompleted(user: TgUser, chatId: TgChatId, currentScore: Score)
}