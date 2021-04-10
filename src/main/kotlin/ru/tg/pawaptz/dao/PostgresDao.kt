package ru.tg.pawaptz.dao

import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.inlined.Answer

interface PostgresDao {

    fun createUserIfNotExist(user: TgUserDto, chatId: TgChatId)

    fun setUserActivityStatus(userDto: TgUserDto, isActive: Boolean = true)

    fun saveTask(task: MathTask): Long

    fun saveAnswer(taskId: Long, userId: Long, answer: Answer, isCorrect: Boolean)

    fun getComplexityOfTaskForUser(tgUserDto: TgUserDto): TaskComplexity?

    fun updateComplexityForUser(tgUserDto: TgUserDto, taskComplexity: TaskComplexity)

    fun getUnResolvedTasks(userDto: TgUserDto, complexity: TaskComplexity, limit: Int = 1): Collection<MathTask>

    fun isUserExists(userDto: TgUserDto): Boolean

    fun getAllActiveUsers(): List<ActiveUser>

    fun setUserActive(tgUserDto: TgUserDto) = setUserActivityStatus(tgUserDto, true)
    fun setUserInActive(tgUserDto: TgUserDto) = setUserActivityStatus(tgUserDto, false)
}