package ru.tg.pawaptz.chats.math.tasks.admin

import kotlinx.coroutines.channels.Channel
import ru.tg.pawaptz.chats.math.tasks.task.MathTask

interface AdminMathTaskChannel {

    fun subscribe(): Channel<MathTask>
}