package ru.tg.pawaptz.chats.math.tasks.gen

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Answer

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CountingTaskComplexityProvider(
    private val dao: PostgresDao,
    channel: ReceiveChannel<UserTaskCompletion>
) : UserComplexityProvider {
    @ObsoleteCoroutinesApi
    private val userContext = newSingleThreadContext("UserContext") + CoroutineExceptionHandler { ctx, t ->
        log.error("Error in context $ctx", t)
    }
    private val userStat = mutableMapOf<TgUser, UserStat>()

    companion object {
        private val log = LoggerFactory.getLogger(CountingTaskComplexityProvider::class.java)
    }

    init {
        CoroutineScope(userContext).launch {
            log.info("Start listening completions")
            while (isActive && !channel.isClosedForReceive) {
                val (activeUser, task, answer) = channel.receive()
                onTaskComplete(task, activeUser.TgUser, answer)
            }
            log.info("Stopped listening completions")
        }
    }

    private fun getComplexityForUser(TgUser: TgUser): TaskComplexity? {
        log.info("Loading initial complexity for user $TgUser")
        return dao.getComplexityOfTaskForUser(TgUser)
    }

    @ObsoleteCoroutinesApi
    override suspend fun startTrackingComplexity(TgUser: TgUser) {
        if (userStat.containsKey(TgUser))
            return
        withContext(userContext) {
            userStat[TgUser] = UserStat().also {
                it.complexity = getComplexityForUser(TgUser) ?: TaskComplexity.EASY
            }
        }
    }

    @ObsoleteCoroutinesApi
    override suspend fun stopTrackingComplexity(TgUser: TgUser) {
        withContext(userContext) {
            userStat.remove(TgUser)
        }
    }

    @ObsoleteCoroutinesApi
    override suspend fun appropriateComplexity(TgUser: TgUser): TaskComplexity = withContext(userContext) {
        userStat[TgUser]?.complexity ?: getComplexityForUser(TgUser) ?: TaskComplexity.EASY
    }

    @ObsoleteCoroutinesApi
    private suspend fun onTaskComplete(task: MathTask, userDto: TgUser, userAnswer: Answer) =
        withContext(userContext) {
            userStat[userDto]?.also {
                kotlin.runCatching { updateUserScores(task, it, userAnswer, userDto) }
                    .onFailure { log.error(it.message, it) }
            }
            Unit
        }

    private fun updateUserScores(
        task: MathTask,
        it: UserStat,
        userAnswer: Answer,
        userDto: TgUser
    ) {
        log.info("User complete the task $task, refreshing complexity")
        val prev = it.complexity
        val newComplexity = if (task.answer() == userAnswer) {
            it.markSuccess()
        } else {
            it.markFail()
        }
        if (prev != newComplexity) {
            log.info("Updating complexity for the user $userDto from $prev to $newComplexity")
            dao.updateComplexityForUser(userDto, newComplexity)
        }
    }

    private class UserStat {
        var complexity: TaskComplexity = TaskComplexity.EASY
        var total = 0
        var success = 0

        fun percentOfSuccess(): Float {
            if (total == 0) return 0.0f
            return (success.toFloat() / total)
        }

        fun markFail(): TaskComplexity {
            total++
            return refresh()
        }

        fun markSuccess(): TaskComplexity {
            total++
            success++
            return refresh()
        }

        private fun refresh(): TaskComplexity {
            if (percentOfSuccess() > 0.9 && total >= 100) {
                val prev = complexity
                complexity = complexity.grow()
                if (prev != complexity) {
                    total = 0
                    success = 0
                }
            }
            return complexity
        }
    }
}