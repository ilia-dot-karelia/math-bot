package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.inlined.Answer

data class UserTaskCompletion(val activeUser: ActiveUser, val task: MathTask, val answer: Answer) {
    fun isSuccessful() = answer.v != Float.MAX_VALUE && task.answer() == answer
}