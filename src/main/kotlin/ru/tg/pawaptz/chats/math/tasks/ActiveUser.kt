package ru.tg.pawaptz.chats.math.tasks

import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUserDto

data class ActiveUser(val tgUserDto: TgUserDto, val chatId: TgChatId) {
    fun id() = tgUserDto.id
}