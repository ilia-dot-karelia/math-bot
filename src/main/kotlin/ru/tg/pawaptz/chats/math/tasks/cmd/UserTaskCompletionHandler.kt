package ru.tg.pawaptz.chats.math.tasks.cmd

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.dao.PostgresDao

class UserTaskCompletionHandler(
    private val dao: PostgresDao,
    private val channel: ReceiveChannel<UserTaskCompletion>
) {
    companion object {
        private val log = LoggerFactory.getLogger(UserSubscriptionHandler::class.java)
    }

    private lateinit var job: Job

    @SuppressWarnings("unused")
    fun stop() {
        job.cancel()
        channel.cancel()
    }

    @SuppressWarnings("unused")
    fun listen() {
        job = CoroutineScope(Dispatchers.Default).launch {
            log.info("Started listening user`s answers")
            while (isActive) {
                val upd = channel.receive()
                log.info("Saving user`s answer: $upd")
                dao.saveAnswer(taskId = upd.task.id(), userId = upd.activeUser.id(), upd.answer, upd.isSuccessful())
            }
            log.info("Stopped listening user`s answers")
        }
    }
}