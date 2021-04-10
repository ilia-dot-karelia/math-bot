package ru.tg.pawaptz.chats.math

import kotlinx.coroutines.channels.ReceiveChannel
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion

interface TgTaskUpdater {

    suspend fun update(userDto: ActiveUser, task: MathTask)

    fun subscribe(): ReceiveChannel<UserTaskCompletion>
}