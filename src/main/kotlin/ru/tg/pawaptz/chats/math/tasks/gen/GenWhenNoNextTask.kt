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
                    dao.saveTask(it, true)
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
            val op = values.first()
            if (op == Operator.DIVIDE && y == 0.0f) {
                values.shuffle()
                continue
            }
            return if (op == Operator.DIVIDE && x.isWhole() && y.isWhole()) {
                if (x == 0.0F) {
                    Operator.DIVIDE.apply(x, y.coerceAtLeast(1.0f))
                } else {
                    val factor = x.toInt().factorize().shuffled().random()
                    Operator.DIVIDE.apply(x / factor, factor.toFloat())
                }
            } else {
                values[0].apply(x, y)
            }
        }
    }
}

private fun Float.isWhole() = this.rem(1.0) == 0.0

private fun Int.factorize(): List<Int> {
    val rez = mutableListOf<Int>()
    for (i in 1..this) {
        if (this.rem(i) == 0) {
            rez.add(i)
        }
    }
    return rez
}