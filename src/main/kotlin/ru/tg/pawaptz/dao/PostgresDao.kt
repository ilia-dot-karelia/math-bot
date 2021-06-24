package ru.tg.pawaptz.dao

import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.inlined.Answer
import ru.tg.pawaptz.inlined.Score

interface PostgresDao {

    fun readTaskCosts(): Map<TaskComplexity, Int>

    fun createUserIfNotExist(user: TgUser, chatId: TgChatId)

    fun setUserActivityStatus(userDto: TgUser, isActive: Boolean = true)

    fun saveTask(task: MathTask): Long

    fun saveAnswer(taskId: Long, userId: Long, answer: Answer)

    fun getComplexityOfTaskForUser(TgUser: TgUser): TaskComplexity?

    fun updateComplexityForUser(TgUser: TgUser, taskComplexity: TaskComplexity)

    fun getUnResolvedTasks(userDto: TgUser, complexity: TaskComplexity, limit: Int = 1): Collection<MathTask>

    fun getUserAnswer(taskId: Long): Float?

    fun isUserExists(userDto: TgUser): Boolean

    fun getAllActiveUsers(): List<Pair<ActiveUser, Score>>

    fun addUserScore(userDto: TgUser, scores: Score)

    fun createUserScoresIfNotExist(userDto: TgUser)

    fun getUserScore(userDto: TgUser): Score?

    fun setUserActive(TgUser: TgUser) = setUserActivityStatus(TgUser, true)

    fun setUserInActive(TgUser: TgUser) = setUserActivityStatus(TgUser, false)
}