package ru.tg.pawaptz.chats.math.tasks.gen

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import ru.tg.api.transport.TgUserDto
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
    private val userContext = newSingleThreadContext("UserContext")
    private val userStat = mutableMapOf<TgUserDto, UserStat>()

    companion object {
        private val log = LoggerFactory.getLogger(CountingTaskComplexityProvider::class.java)
    }

    init {
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            log.info("Start listening completions")
            while (isActive && !channel.isClosedForReceive) {
                val (activeUser, task, answer) = channel.receive()
                onTaskComplete(task, activeUser.tgUserDto, answer)
            }
            log.info("Stopped listening completions")
        }
    }

    private fun getComplexityForUser(tgUserDto: TgUserDto): TaskComplexity? {
        log.info("Loading initial complexity for user $tgUserDto")
        return dao.getComplexityOfTaskForUser(tgUserDto)
    }

    @ObsoleteCoroutinesApi
    override suspend fun startTrackingComplexity(tgUserDto: TgUserDto) {
        if (userStat.containsKey(tgUserDto))
            return
        withContext(userContext) {
            userStat[tgUserDto] = UserStat().also {
                it.complexity = getComplexityForUser(tgUserDto) ?: TaskComplexity.EASY
            }
        }
    }

    @ObsoleteCoroutinesApi
    override suspend fun stopTrackingComplexity(tgUserDto: TgUserDto) {
        withContext(userContext) {
            userStat.remove(tgUserDto)
        }
    }

    @ObsoleteCoroutinesApi
    override suspend fun appropriateComplexity(tgUserDto: TgUserDto): TaskComplexity = withContext(userContext) {
        userStat[tgUserDto]?.complexity ?: getComplexityForUser(tgUserDto) ?: TaskComplexity.EASY
    }

    @ObsoleteCoroutinesApi
    private suspend fun onTaskComplete(task: MathTask, userDto: TgUserDto, userAnswer: Answer) =
        withContext(userContext) {
            userStat[userDto]?.also {
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
            Unit
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