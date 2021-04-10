package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity

interface TaskGenStrategy {

    fun generate(userDto: TgUserDto, complexity: TaskComplexity): Sequence<MathTask>
}