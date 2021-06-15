import ru.tg.api.inlined.*
import ru.tg.api.transport.TgChat
import ru.tg.api.transport.TgUser

val anyChat: TgChat = TgChat(1, ChatType("type"), Title("title"), UserName("user"), FirstName("name"), LastName("last"))
val anyUser: TgUser = TgUser(1, false, FirstName("test"))
