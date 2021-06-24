package ru.tg.pawaptz.chats.math.tasks.gen

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import ru.tg.pawaptz.chats.math.TgTaskUpdater
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.TaskCosts
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.inlined.Answer
import ru.tg.pawaptz.inlined.Score

@ExperimentalCoroutinesApi
class UserTaskManagerImpl(
    private val complexityProvider: UserComplexityProvider,
    private val strategy: TaskGenStrategy,
    private val taskUpdater: TgTaskUpdater,
    private val dao: PostgresDao,
    private val channel: ReceiveChannel<UserTaskCompletion>,
    private val taskCosts: TaskCosts
) : UserTaskManager {

    private val userMap = mutableMapOf<ActiveUser, UserContext>()

    private val mtx = Mutex()
    private lateinit var job: Job

    companion object {
        private val log = LoggerFactory.getLogger(UserTaskManager::class.java)
    }

    override fun stop() {
        job.cancel("Externally stopped")
    }

    override fun start() {
        restoreActiveUsers()
        job = CoroutineScope(Dispatchers.Default).launch {
            log.info("Start listening for task completions")
            while (isActive && !channel.isClosedForReceive) {
                kotlin.runCatching { onTaskComplete(channel.receive()) }
                    .onFailure { log.error("Failed to handle task completion event ${it.message}", it) }
            }
            log.info("Stopped listening for task completions")
        }
    }

    private fun restoreActiveUsers() = runBlocking {
        dao.getAllActiveUsers().forEach {
            startManageUserTasks(it.first, it.second)
        }
    }

    override suspend fun startManageUserTasks(activeUser: ActiveUser, score: Score) {
        mtx.withLock {
            if (userMap.containsKey(activeUser)) {
                return@withLock
            }
            log.info("Starting managing of user $activeUser")
            complexityProvider.startTrackingComplexity(activeUser.TgUser)
            val complexity = complexityProvider.appropriateComplexity(activeUser.TgUser)
            log.info("Using complexity $complexity for user $activeUser")
            userMap[activeUser] = UserContext(score, complexity, dao, activeUser.TgUser, strategy)
        }
        sendNext(activeUser)
    }

    override suspend fun completeUserTaskManagement(activeUser: ActiveUser) = mtx.withLock {
        if (!userMap.containsKey(activeUser)) {
            return@withLock
        }
        log.info("Stop managing of the user $activeUser")
        userMap.remove(activeUser)
        complexityProvider.stopTrackingComplexity(activeUser.TgUser)
    }

    private suspend fun sendNext(activeUser: ActiveUser) {
        val task: MathTask? = userMap[activeUser]?.nextTask()
        if (task != null)
            CoroutineScope(Dispatchers.Default).launch { taskUpdater.sendTaskAndWaitAnswer(activeUser, task) }
    }

    private suspend fun onTaskComplete(upd: UserTaskCompletion) = mtx.withLock {
        val userAnswer = upd.answer
        log.info("Task is complete by user $userAnswer, sending the next one")
        if (upd.answer !is Answer.NoAnswer) {
            dao.saveAnswer(taskId = upd.task.id(), userId = upd.activeUser.id(), userAnswer)
            log.info("Saved user`s answer: $userAnswer")
            val cost = taskCosts.getCost(upd.task.complexity())
            val scoresForTask = Score(if (userAnswer.isCorrect()) cost else -cost)
            userMap[upd.activeUser]?.addScores(scoresForTask)
        }
        delay(3_000)
        sendNext(upd.activeUser)
    }
}