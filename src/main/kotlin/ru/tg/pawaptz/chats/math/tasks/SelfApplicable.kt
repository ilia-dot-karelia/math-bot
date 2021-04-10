package ru.tg.pawaptz.chats.math.tasks

import ru.tg.pawaptz.chats.math.tasks.task.MathTask

interface SelfApplicable {

    fun apply(x: Float, y: Float): MathTask
}