package ru.tg.pawaptz.chats.math.tasks.cmd

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgMessageDto
import ru.tg.api.transport.TgUpdateDto
import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.gen.UserTaskManager
import ru.tg.pawaptz.dao.PostgresDao

class UserSubscriptionHandler(
    private val channel: ReceiveChannel<TgUpdateDto>,
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
        msg: TgMessageDto,
        user: TgUserDto = msg.from,
        tgChatId: TgChatId
    ) = kotlin.runCatching {
        dao.createUserIfNotExist(user, tgChatId)
        val activeUser = ActiveUser(user, tgChatId)
        if (msg.isSubscribeMessage()) {
            dao.setUserActive(user)
            userTaskManager.startManageUserTasks(activeUser)
        } else if (msg.isUnSubscribeMessage()) {
            dao.setUserInActive(user)
            userTaskManager.completeUserTaskManagement(activeUser)
        }
    }.onFailure {
        log.error(it.message, it)
    }.getOrNull()

    private fun TgMessageDto.isSubscribeMessage(): Boolean {
        return this.text == subscribe
    }

    private fun TgMessageDto.isUnSubscribeMessage(): Boolean {
        return this.text == unSubscribe
    }
}