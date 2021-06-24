package ru.tg.pawaptz.chats.math

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import ru.tg.api.generic.TgBot
import ru.tg.api.inlined.TgChatId
import ru.tg.api.inlined.TgMessageId
import ru.tg.api.inlined.TgText
import ru.tg.api.poll.TgQuizOptions
import ru.tg.api.transport.TgCallbackQuery
import ru.tg.api.transport.TgUpdate
import ru.tg.pawaptz.achievments.FunnySmiles
import ru.tg.pawaptz.achievments.SadSmiles
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.MathTaskWithRandomizedOptions
import ru.tg.pawaptz.chats.math.tasks.task.UserTaskCompletion
import ru.tg.pawaptz.inlined.Answer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalTime
class TgTaskUpdaterImpl(private val tgBot: TgBot) : TgTaskUpdater {

    companion object {
        private val log = LoggerFactory.getLogger(TgTaskUpdaterImpl::class.java)
    }

    @ExperimentalCoroutinesApi
    private val channel = BroadcastChannel<UserTaskCompletion>(800)

    @ExperimentalCoroutinesApi
    override suspend fun sendTaskAndWaitAnswer(activeUser: ActiveUser, task: MathTask) {
        log.info("Sending the task: $task")
        val quizTask = MathTaskWithRandomizedOptions(task)
        val options = quizTask.options(4)
        val subscription = tgBot.subscribe()
        val sentMessage = tgBot.sendMessage(activeUser.chatId, TgText(quizTask.mathTask().description().question())) {
            inlineKeyBoard {
                createNewLine {
                    options.stringList().forEachIndexed { index, title ->
                        addButton(title, index.toString())
                    }
                }
            }
        }
        log.info("Task is sent: $sentMessage")
        val res = withTimeoutOrNull(Duration.Companion.minutes(100000)) {
            lateinit var upd: TgUpdate
            do {
                upd = subscription.receive()
            } while (upd.callbackQuery?.message?.msgId ?: Long.MIN_VALUE != sentMessage.msgId)
            handleResponse(upd, activeUser, task, options)
        }
        if (res == null) {
            channel.trySend(UserTaskCompletion(activeUser, task, Answer.NoAnswer))
        }
    }

    private suspend fun handleResponse(
        upd: TgUpdate,
        activeUser: ActiveUser,
        task: MathTask,
        options: TgQuizOptions
    ) {
        log.info("Received Answer: $upd")
        val tgCallbackQuery = upd.callbackQuery as TgCallbackQuery
        val selected = tgCallbackQuery.data.toInt()
        val completion =
            UserTaskCompletion(
                activeUser,
                task,
                Answer.ofTask(task, options.option(selected).v.toFloat())
            )
        markTask(
            tgCallbackQuery.message!!.msgId,
            TgChatId(tgCallbackQuery.message!!.chat.id),
            completion
        )
        channel.send(
            completion
            // todo support any type of options
        )
    }

    @ExperimentalCoroutinesApi
    override fun subscribe(): ReceiveChannel<UserTaskCompletion> {
        return channel.openSubscription()
    }


    private suspend fun markTask(messageId: TgMessageId, chatId: TgChatId, taskCompletion: UserTaskCompletion) {
        val text = TgText(
            "${chooseEmoji(taskCompletion)} ${
                taskCompletion.task.description().withAnswer(taskCompletion.answer)
            }"
        )
        tgBot.editMessage(chatId, messageId, text) {
            disableWebPagePreview = true
        }
    }

    private fun chooseEmoji(userTaskCompletion: UserTaskCompletion): String {
        return if (userTaskCompletion.isSuccessful())
            FunnySmiles.values().random().emoji().unicode
        else
            SadSmiles.values().random().emoji().unicode
    }
}