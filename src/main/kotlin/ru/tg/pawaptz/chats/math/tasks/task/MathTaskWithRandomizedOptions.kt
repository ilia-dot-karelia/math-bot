package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.api.poll.TgPoll
import ru.tg.api.poll.TgQuizOptions
import kotlin.random.Random

class MathTaskWithRandomizedOptions(private val task: MathTask) : MathTaskWithOptions {

    override fun mathTask(): MathTask = task

    override fun options(size: Int): TgQuizOptions {
        val correct = task.answer().v.toInt() //todo support floating point options
        val used = mutableSetOf<Int>()
        val cO = TgPoll.TgQuizOption.CorrectOption(correct.toString())
        used.add(correct)
        val rez = mutableListOf(*Array<TgPoll.TgQuizOption>(size) { cO })

        val interval = (2 * correct).coerceAtLeast(size)
        val corrIdx = Random.nextInt(0, size)
        var idx = 0
        while (idx < rez.size) {
            if (idx == corrIdx) {
                idx++
                continue
            }
            val option = Random.nextInt(0, interval)
            if (!used.contains(option)) {
                rez[idx++] = TgPoll.TgQuizOption.IncorrectOption(option.toString())
            }
        }
        return TgQuizOptions(rez)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MathTaskWithRandomizedOptions

        if (task != other.task) return false

        return true
    }

    override fun hashCode(): Int {
        return task.hashCode()
    }

    override fun toString(): String {
        return "MathTaskWithRandomizedOptions(task=$task)"
    }
}