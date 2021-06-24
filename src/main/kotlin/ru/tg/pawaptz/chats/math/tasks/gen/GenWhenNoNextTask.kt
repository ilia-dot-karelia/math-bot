package ru.tg.pawaptz.chats.math.tasks.gen

import org.slf4j.LoggerFactory
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.Operator
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.dao.PostgresDao

class GenWhenNoNextTask(private val dao: PostgresDao) : TaskGenStrategy {

    companion object {
        private val log = LoggerFactory.getLogger(GenWhenNoNextTask::class.java)
    }

    override fun generate(userDto: TgUser, complexity: TaskComplexity): Sequence<MathTask> = sequence {
        val unResolvedTasks = dao.getUnResolvedTasks(userDto, complexity, 10)
        if (unResolvedTasks.isEmpty()) {
            val task = nextRnd(complexity)
            kotlin.runCatching {
                val taskId = task.let {
                    dao.saveTask(it)
                }
                if (dao.getUserAnswer(taskId) != task.answer().v) {
                    log.info("Yielding the next random task $taskId")
                    yield(task.replaceId(taskId))
                }
            }.onFailure { log.error(it.message, it); }
        } else {
            log.info("Yielding unresolved tasks, example ${unResolvedTasks.first().id()}")
            yieldAll(unResolvedTasks)
        }
    }

    private fun nextRnd(complexity: TaskComplexity): MathTask {
        val values = Operator.values()
        values.shuffle()

        val y = complexity.generate()
        val x = complexity.generate()
        while (true) {
            if (values.first() != Operator.DIVIDE || y != 0.0f) {
                return values[0].apply(x, y)
            } else {
                values.shuffle()
            }
        }
    }
}