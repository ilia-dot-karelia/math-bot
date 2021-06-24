package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.pawaptz.inlined.Answer

class MathIntTaskDescription(private val description: String) : TaskDescription {

    private val regex = ".0(\$|\\s)".toRegex()
    override fun question(): String {
        return description.replace(regex, " ").trim()
    }

    override fun withAnswer(answer: Answer): String {
        return "$description = ${answer.v.toInt()}"
    }

    override fun toString(): String {
        return "MathIntTaskDescription(description='$description', regex=$regex)"
    }
}