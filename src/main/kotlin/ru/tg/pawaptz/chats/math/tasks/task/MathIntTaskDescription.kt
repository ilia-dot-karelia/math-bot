package ru.tg.pawaptz.chats.math.tasks.task

class MathIntTaskDescription(private val description: String) : TaskDescription {

    private val regex = ".0(\$|\\s)".toRegex()
    override fun get(): String {
        return description
        //return regex.replace(description, " ").trimEnd()
    }

    override fun toString(): String {
        return "MathIntTaskDescription(description='$description', regex=$regex)"
    }
}