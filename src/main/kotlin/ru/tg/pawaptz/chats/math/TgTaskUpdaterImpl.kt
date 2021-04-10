package ru.tg.pawaptz.chats.math

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import org.slf4j.LoggerFactory
import ru.tg.api.generic.TgBot
import ru.tg.api.inlined.TgChatId
import ru.tg.api.inlined.TgPollExplanation
import ru.tg.api.inlined.TgPollQuestion
import ru.tg.api.poll.TgPoll
import ru.tg.api.poll.TgPollType
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.MathTaskWithRandomizedOptions
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.inlined.Answer
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class TgTaskUpdaterImpl(private val tgBot: TgBot) : TgTaskUpdater {

    companion object {
        private val log = LoggerFactory.getLogger(TgTaskUpdaterImpl::class.java)
    }

    @ExperimentalCoroutinesApi
    private val channel = BroadcastChannel<UserTaskCompletion>(800)

    @ExperimentalCoroutinesApi
    override suspend fun update(userDto: ActiveUser, task: MathTask) {
        CoroutineScope(Dispatchers.Default).launch {
            kotlin.runCatching {
                log.info("Sending the task: $task")
                val quizTask = MathTaskWithRandomizedOptions(task)
                val poll = TgPoll(
                    TgChatId(115417017),
                    TgPollQuestion(quizTask.mathTask().description().get()),
                    quizTask.options(3),
                    TgPollType.QUIZ,
                    TgPollExplanation(""),
                    2.toDuration(DurationUnit.HOURS),
                    isAnonymous = false
                )
                tgBot.sendPoll(poll)
                val updateChannel = poll.updateChannel()
                launch asd@{
                    repeat(100) {
                        if (isActive) {
                            val answer = updateChannel.poll()
                            if (answer != null) {
                                log.info("Received Answer: $answer")
                                if (answer.selectedOptions.size > 1) {
                                    throw IllegalStateException("Selected more than 1 option, $answer")
                                }
                                val selected = answer.selectedOptions.first()
                                channel.offer(
                                    UserTaskCompletion(userDto, task, Answer(poll.options.option(selected).v.toFloat()))
                                    // todo support any type of options
                                )
                                return@asd
                            }
                            delay(2_000)
                        }
                    }
                    channel.offer(UserTaskCompletion(userDto, task, Answer(Float.MAX_VALUE)))
                }
            }.onFailure { log.error(it.message, it) }
        }
    }

    @ExperimentalCoroutinesApi
    override fun subscribe(): ReceiveChannel<UserTaskCompletion> {
        return channel.openSubscription()
    }
}