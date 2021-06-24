package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.inlined.Score

interface UserTaskManager {

    suspend fun startManageUserTasks(activeUser: ActiveUser, score: Score)

    suspend fun completeUserTaskManagement(activeUser: ActiveUser)
    fun start()
    fun stop()
}