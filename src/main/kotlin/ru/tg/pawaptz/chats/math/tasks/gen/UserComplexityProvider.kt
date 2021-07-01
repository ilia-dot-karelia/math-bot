package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity

interface UserComplexityProvider {

    suspend fun startTrackingComplexity(TgUser: TgUser)
    suspend fun stopTrackingComplexity(TgUser: TgUser)
    suspend fun userComplexity(TgUser: TgUser): TaskComplexity
}