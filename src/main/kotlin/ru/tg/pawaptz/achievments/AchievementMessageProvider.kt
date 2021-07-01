package ru.tg.pawaptz.achievments

import ru.tg.pawaptz.inlined.Score

interface AchievementMessageProvider {

    fun get(mood: Mood, currentScore: Score): String
}
