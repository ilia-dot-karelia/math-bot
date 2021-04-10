package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.dao.PostgresDao

class GenWhenNoNextTask(private val genStrategy: TaskGenStrategy, private val dao: PostgresDao) : TaskGenStrategy {
    override fun generate(userDto: TgUserDto, complexity: TaskComplexity): Sequence<MathTask> = sequence {
        val unResolvedTasks = dao.getUnResolvedTasks(userDto, complexity, 10)
        if (unResolvedTasks.isEmpty()) {
            genStrategy.generate(userDto, complexity).forEach {
                val savedTaskId = dao.saveTask(it)
                yield(it.replaceId(savedTaskId))
            }
        } else {
            yieldAll(unResolvedTasks)
        }
    }
}