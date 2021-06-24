package ru.tg.pawaptz.chats.math.tasks

import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity

data class TaskCosts(private val costMap: Map<TaskComplexity, Int>) {

    fun getCost(complexity: TaskComplexity) =
        costMap[complexity] ?: throw IllegalStateException("complexity $complexity is not supported")
}