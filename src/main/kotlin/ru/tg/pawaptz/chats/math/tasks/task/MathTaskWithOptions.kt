package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.api.poll.TgQuizOptions

interface MathTaskWithOptions {

    fun mathTask(): MathTask

    fun options(size: Int): TgQuizOptions
}