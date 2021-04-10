package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.pawaptz.inlined.Answer

interface MathTask {

    fun id(): Long

    fun description(): TaskDescription

    fun answer(): Answer

    fun complexity(): TaskComplexity

    fun replaceId(id: Long): MathTask

    fun isGenerated(): Boolean
}