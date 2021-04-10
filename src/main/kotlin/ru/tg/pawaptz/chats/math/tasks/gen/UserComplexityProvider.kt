package ru.tg.pawaptz.chats.math.tasks.gen

import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity

interface UserComplexityProvider {

    suspend fun startTrackingComplexity(tgUserDto: TgUserDto)
    suspend fun stopTrackingComplexity(tgUserDto: TgUserDto)

    suspend fun appropriateComplexity(tgUserDto: TgUserDto): TaskComplexity
}