package ru.tg.pawaptz.chats.math

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import ru.tg.api.generic.TgBot
import ru.tg.api.generic.TgBotImpl
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.achievments.AchievementMessageProvider
import ru.tg.pawaptz.achievments.AchievementMessageProviderImpl
import ru.tg.pawaptz.achievments.AchievementSender
import ru.tg.pawaptz.achievments.AchievementSenderImpl
import ru.tg.pawaptz.chats.math.tasks.TaskCosts
import ru.tg.pawaptz.chats.math.tasks.admin.AdminMathTaskChannel
import ru.tg.pawaptz.chats.math.tasks.admin.AdminMathTaskReceiver
import ru.tg.pawaptz.chats.math.tasks.admin.AdminTaskProcessor
import ru.tg.pawaptz.chats.math.tasks.cmd.UserCommandHandler
import ru.tg.pawaptz.chats.math.tasks.gen.*
import ru.tg.pawaptz.dao.PostgresDao
import ru.tg.pawaptz.dao.PostgresDaoImpl
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@SuppressWarnings("unused")
@SpringBootConfiguration
@PropertySource("file:private/secrets.properties")
class Config {

    @ExperimentalCoroutinesApi
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun userManager(
        complexityProvider: UserComplexityProvider,
        tgTaskUpdater: TgTaskUpdater,
        dao: PostgresDao,
        taskCosts: TaskCosts,
        achievementSender: AchievementSender
    ): UserTaskManager {
        return UserTaskManagerImpl(
            complexityProvider,
            GenWhenNoNextTask(dao),
            tgTaskUpdater,
            dao,
            tgTaskUpdater.subscribe(),
            taskCosts,
            achievementSender
        )
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Bean
    fun userComplexityProvider(dao: PostgresDao, tgTaskUpdater: TgTaskUpdater): UserComplexityProvider {
        return CountingTaskComplexityProvider(dao, tgTaskUpdater.subscribe())
    }

    @Bean
    @ExperimentalTime
    @ExperimentalCoroutinesApi
    fun tgBot(@Value("\${tg.bot.token}") token: String): TgBot {
        return TgBotImpl.create(token)
    }

    @Bean
    @ExperimentalTime
    fun taskUpdater(tgBot: TgBot): TgTaskUpdater =
        TgTaskUpdaterImpl(tgBot = tgBot)

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ExperimentalCoroutinesApi
    fun adminTaskChannel(tgBot: TgBot, adminUser: TgUser): AdminMathTaskChannel {
        return AdminMathTaskReceiver(tgBot.subscribe(), adminUser)
    }

    @Bean(initMethod = "start")
    fun adminTaskProcessor(
        adminMathTaskChannel: AdminMathTaskChannel,
        postgresDao: PostgresDao,
        adminUser: TgUser,
        @Value("\${tg.admin.chatId}") adminChatId: Int
    ): AdminTaskProcessor {
        return AdminTaskProcessor(adminMathTaskChannel, postgresDao, adminUser, TgChatId(adminChatId))
    }

    @ExperimentalCoroutinesApi
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun userSubscriptionHandler(
        tgBot: TgBot,
        postgresDao: PostgresDao,
        userTaskManager: UserTaskManager
    ): UserCommandHandler {
        return UserCommandHandler(tgBot.subscribe(), postgresDao, userTaskManager, tgBot)
    }

    @Bean
    fun adminUser(
        @Value("\${tg.admin.id}") adminId: Long,
        @Value("\${tg.admin.name}") adminName: String
    ): TgUser {
        return TgUser(adminId, false, FirstName(adminName))
    }

    @Bean
    fun postgresDao(template: JdbcTemplate, tm: DataSourceTransactionManager, tx: TransactionTemplate): PostgresDao {
        return PostgresDaoImpl(template)
    }

    @Bean
    fun taskCosts(postgresDao: PostgresDao): TaskCosts {
        return TaskCosts(postgresDao.readTaskCosts())
    }

    @Bean
    fun achieveMessageProvider(postgresDao: PostgresDao): AchievementMessageProvider {
        return AchievementMessageProviderImpl(postgresDao)
    }

    @Bean
    fun achievementSender(
        tgBot: TgBot,
        complexityProvider: UserComplexityProvider,
        taskCosts: TaskCosts,
        achievementMessageProvider: AchievementMessageProvider
    ): AchievementSender {
        return AchievementSenderImpl(3, tgBot, complexityProvider, taskCosts, achievementMessageProvider)
    }
}