package ru.tg.pawaptz.achievments

import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Score

class AchievementMessageProviderImpl(private val dao: PostgresDao) : AchievementMessageProvider {

    override fun get(mood: Mood, currentScore: Score): String {
        val achieveMessage = dao.getAchieveMessage(mood)
        val emoji = BasicSmiles.values().random().emoji()
        return "${emoji.unicode} $achieveMessage, your current score is $currentScore"
    }
}