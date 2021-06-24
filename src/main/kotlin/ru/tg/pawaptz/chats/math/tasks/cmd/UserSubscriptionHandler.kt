package ru.tg.pawaptz.chats.math.tasks.cmd

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgMessage
import ru.tg.api.transport.TgUpdate
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.gen.UserTaskManager
import ru.tg.pawaptz.dao.PostgresDao

class UserSubscriptionHandler(
    private val channel: ReceiveChannel<TgUpdate>,
    private val dao: PostgresDao,
    private val userTaskManager: UserTaskManager
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserSubscriptionHandler::class.java)
        const val subscribe = "/subscribe"
        const val unSubscribe = "/unsubscribe"
    }

    @ExperimentalCoroutinesApi
    fun start() {
        CoroutineScope(Dispatchers.Default).launch {
            log.info("Start listening for the users commands")
            while (isActive && !channel.isClosedForReceive) {
                val msg = channel.receive().message
                if (msg != null) {
                    handle(msg, msg.from, TgChatId(msg.chat.id))
                }
            }
            log.info("Stopping to listen for the users commands")
        }
    }

    fun stop() {
        channel.cancel()
    }

    private suspend fun handle(
        msg: TgMessage,
        user: TgUser = msg.from,
        tgChatId: TgChatId
    ) = kotlin.runCatching {
        dao.createUserIfNotExist(user, tgChatId)
        dao.createUserScoresIfNotExist(user)
        val activeUser = ActiveUser(user, tgChatId)
        if (msg.isSubscribeMessage()) {
            dao.setUserActive(user)
            val userScore = dao.getUserScore(user) ?: throw IllegalStateException("Scores not init")
            userTaskManager.startManageUserTasks(activeUser, userScore)
        } else if (msg.isUnSubscribeMessage()) {
            dao.setUserInActive(user)
            userTaskManager.completeUserTaskManagement(activeUser)
        }
    }.onFailure {
        log.error(it.message, it)
    }.getOrNull()

    private fun TgMessage.isSubscribeMessage(): Boolean {
        return this.text == subscribe
    }

    private fun TgMessage.isUnSubscribeMessage(): Boolean {
        return this.text == unSubscribe
    }
}