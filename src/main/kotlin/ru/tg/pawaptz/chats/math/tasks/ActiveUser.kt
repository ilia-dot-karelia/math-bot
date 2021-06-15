package ru.tg.pawaptz.chats.math.tasks

import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser

data class ActiveUser(val TgUser: TgUser, val chatId: TgChatId) {
    fun id() = TgUser.id
}