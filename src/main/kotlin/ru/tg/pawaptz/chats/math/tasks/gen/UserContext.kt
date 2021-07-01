package ru.tg.pawaptz.chats.math.tasks.gen

import kotlinx.coroutines.Job
import org.slf4j.LoggerFactory
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Score
import java.util.concurrent.CancellationException

internal class UserContext(
    internal var score: Score,
    internal var complexity: TaskComplexity,
    private val dao: PostgresDao,
    private val usr: TgUser,
    private val taskGenStrategy: TaskGenStrategy
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserContext::class.java)
    }

    fun close() {
        taskSendingJob?.cancel(CancellationException("User[$usr] context closed"))
    }

    internal var taskSendingJob: Job? = null

    private var taskIterator = taskGenStrategy.generate(usr, complexity).iterator()

    fun nextTask(): MathTask {
        return kotlin.runCatching { nextInternal() }
            .onFailure { log.error(it.message, it) }
            .recover { nextInternal() }
            .getOrThrow()
    }

    private fun nextInternal(): MathTask {
        while (!taskIterator.hasNext())
            taskIterator = taskGenStrategy.generate(usr, complexity).iterator()
        return taskIterator.next()
    }

    fun addScores(score: Score): Score {
        return dao.addUserScore(usr, score)
    }
}