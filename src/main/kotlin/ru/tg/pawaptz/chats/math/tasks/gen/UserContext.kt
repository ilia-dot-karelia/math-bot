package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Score

internal class UserContext(
    internal var score: Score,
    internal var complexity: TaskComplexity,
    private val dao: PostgresDao,
    private val usr: TgUser,
    private val taskGenStrategy: TaskGenStrategy
) {

    private var taskIterator = taskGenStrategy.generate(usr, complexity).iterator()

    fun nextTask(): MathTask {
        if (!taskIterator.hasNext())
            taskIterator = taskGenStrategy.generate(usr, complexity).iterator()
        return taskIterator.next()
    }

    fun addScores(score: Score) {
        dao.addUserScore(usr, score)
    }
}