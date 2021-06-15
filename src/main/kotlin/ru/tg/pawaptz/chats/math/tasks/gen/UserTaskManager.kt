package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.pawaptz.chats.math.tasks.ActiveUser

interface UserTaskManager {

    suspend fun startManageUserTasks(activeUser: ActiveUser)

    suspend fun completeUserTaskManagement(activeUser: ActiveUser)
    fun start()
    fun stop()
}