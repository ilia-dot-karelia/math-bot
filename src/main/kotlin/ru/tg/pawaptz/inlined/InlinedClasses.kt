package ru.tg.pawaptz.inlined

import ru.tg.pawaptz.chats.math.tasks.task.MathTask

sealed class Answer(val v: Float) {

    companion object {
        fun ofTask(tsk: MathTask, userAnswer: Float): Answer {
            check(tsk.answer() !is NoAnswer)
            return if (tsk.answer().v == userAnswer)
                CorrectAnswer(userAnswer)
            else InCorrectAnswer(userAnswer)
        }
    }

    abstract fun isCorrect(): Boolean

    override fun toString(): String {
        return "Answer(v=$v)"
    }

    object NoAnswer : Answer(Float.MAX_VALUE) {
        override fun isCorrect(): Boolean = false
    }

    class InCorrectAnswer(v: Float) : Answer(v) {
        override fun isCorrect(): Boolean = false
    }

    class CorrectAnswer(v: Float) : Answer(v) {
        override fun isCorrect(): Boolean = true
    }
}

@JvmInline
value class Score(val v: Int) {
    companion object {
        val ZERO = Score(0)
    }

    operator fun plus(other: Score): Score {
        return Score(v + other.v)
    }

    operator fun times(other: Int): Score {
        return Score(v * other)
    }

    override fun toString(): String {
        return "$v"
    }
}
