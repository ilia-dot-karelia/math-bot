package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.pawaptz.inlined.Answer

data class SimpleMathTask(
    private val taskId: Long = Long.MAX_VALUE,
    private val taskDescription: TaskDescription,
    private val complexity: TaskComplexity = TaskComplexity.EASY,
    private val answer: Answer,
    private val isGenerated: Boolean = false
) : MathTask {

    override fun id(): Long {
        return taskId
    }

    override fun description(): TaskDescription = taskDescription
    override fun answer(): Answer = answer
    override fun complexity(): TaskComplexity = complexity
    override fun replaceId(id: Long): MathTask {
        return copy(
            taskId = id,
            taskDescription = taskDescription,
            complexity = complexity,
            answer = answer,
            isGenerated = isGenerated
        )
    }

    override fun isGenerated(): Boolean {
        return isGenerated
    }
}
