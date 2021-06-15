package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.pawaptz.inlined.Answer

interface TaskDescription {
    fun question(): String

    fun withAnswer(answer: Answer): String
}