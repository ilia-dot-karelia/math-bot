package ru.tg.pawaptz.chats.math.tasks.admin

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.dao.PostgresDao

class AdminTaskProcessor(
    private val adminMathTaskChannel: AdminMathTaskChannel,
    private val postgresDao: PostgresDao,
    private val adminUser: TgUserDto,
    private val adminChatId: TgChatId
) {

    companion object {
        private val log = LoggerFactory.getLogger(AdminTaskProcessor::class.java)
    }

    @ExperimentalCoroutinesApi
    fun start() {
        postgresDao.createUserIfNotExist(adminUser, adminChatId)
        log.info("Subscribing to admin task channel update")
        CoroutineScope(Dispatchers.Default).launch {
            val channel = adminMathTaskChannel.subscribe()
            while (isActive && !channel.isClosedForReceive) {
                val nextTask = channel.receive()
                log.info("Received the next admin math task: $nextTask")
                postgresDao.saveTask(nextTask)
            }
            log.info("Admin channel is closed, stop processing admin tasks")
        }
        log.info("Subscribed to admin task channel update")
    }
}