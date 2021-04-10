package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.Operator
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity

class RandomNextTaskStrategy : TaskGenStrategy {

    override fun generate(userDto: TgUserDto, complexity: TaskComplexity): Sequence<MathTask> = sequence {
        while (true) {
            Operator.values().forEach {
                val y = complexity.generate()
                val x = complexity.generate()
                if (it != Operator.DIVIDE || y != 0.0f) {
                    yield(it.apply(x, y))
                }
            }
        }
    }
}