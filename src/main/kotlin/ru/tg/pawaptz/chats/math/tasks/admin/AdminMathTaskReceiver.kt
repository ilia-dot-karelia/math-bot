package ru.tg.pawaptz.chats.math.tasks.admin

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import ru.tg.api.transport.TgMessage
import ru.tg.api.transport.TgUpdate
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.inlined.Answer

@ExperimentalCoroutinesApi
class AdminMathTaskReceiver(
    private val channel: ReceiveChannel<TgUpdate>,
    private val admin: TgUser
) : AdminMathTaskChannel {
    companion object {
        private val log = LoggerFactory.getLogger(AdminMathTaskReceiver::class.java)
    }

    private val taskChannel = Channel<MathTask>()
    private lateinit var job: Job

    fun start() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && !channel.isClosedForReceive) {
                val update = channel.receive().message

                if (update != null) {
                    if (update.from == admin && update.text.startsWith("#")) {
                        log.info("[admin] Received update: $update")
                        kotlin.runCatching { parseTask(update) }
                            .onFailure {
                                log.error(
                                    "Unable to recognize admin task (update=$update), ${it.message}",
                                    it
                                )
                            }.onSuccess { taskChannel.send(it) }
                    }
                }
            }
        }
    }

    fun stop() {
        job.cancel()
    }

    private fun parseTask(upd: TgMessage): MathTask {
        val split = upd.text.split(";")
        val task = MathIntTaskDescription(split[0].substring(1))
        val complexity = TaskComplexity.valueOf(split[1])
        val answer = Answer.CorrectAnswer(split[2].toFloat())
        return SimpleMathTask(taskDescription = task, complexity = complexity, answer = answer, isGenerated = false)
    }

    override fun subscribe(): Channel<MathTask> = taskChannel
}