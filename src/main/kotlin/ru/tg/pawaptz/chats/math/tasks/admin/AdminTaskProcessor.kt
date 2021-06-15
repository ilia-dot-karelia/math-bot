package ru.tg.pawaptz.chats.math.tasks.admin

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.dao.PostgresDao

class AdminTaskProcessor(
    private val adminMathTaskChannel: AdminMathTaskChannel,
    private val postgresDao: PostgresDao,
    private val adminUser: TgUser,
    private val adminChatId: TgChatId
) {

    companion object {
        private val log = LoggerFactory.getLogger(AdminTaskProcessor::class.java)
    }

    @ExperimentalCoroutinesApi
    fun start() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            log.error("Exception in thread ${t.name}: ${e.message}", e)
        }
        postgresDao.createUserIfNotExist(adminUser, adminChatId)
        log.info("Subscribing to admin task channel update")
        CoroutineScope(Dispatchers.Default).launch {
            val channel = adminMathTaskChannel.subscribe()
            while (isActive && !channel.isClosedForReceive) {
                val nextTask = channel.receive()
                log.info("Received the next admin math task: $nextTask")
                kotlin.runCatching { postgresDao.saveTask(nextTask) }
                    .onFailure { log.error(it.message, it) }
            }
            log.info("Admin channel is closed, stop processing admin tasks")
        }
        log.info("Subscribed to admin task channel update")
    }
}