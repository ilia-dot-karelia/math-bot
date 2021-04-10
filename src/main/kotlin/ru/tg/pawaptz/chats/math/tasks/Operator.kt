package ru.tg.pawaptz.chats.math.tasks;

import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.inlined.Answer

enum class Operator(val sign: String) : SelfApplicable {
    PLUS("+") {
        override fun apply(x: Float, y: Float): MathTask {
            return SimpleMathTask(
                taskDescription = MathIntTaskDescription("$x ${this.sign} $y = ?"),
                answer = Answer(x + y),
                isGenerated = true
            )
        }
    },
    MINUS("-") {
        override fun apply(x: Float, y: Float): MathTask {
            val max = x.coerceAtLeast(y)
            val min = x.coerceAtMost(y)
            return SimpleMathTask(
                taskDescription = MathIntTaskDescription("$max ${this.sign} $min = ?"),
                answer = Answer(max - min),
                isGenerated = true
            )
        }
    },
    DIVIDE("/") {
        override fun apply(x: Float, y: Float): MathTask {
            val multi = x * y
            return SimpleMathTask(
                taskDescription = MathIntTaskDescription("$multi ${this.sign} $y = ?"),
                answer = Answer(multi / y),
                isGenerated = true
            )
        }
    },
    MULTIPLY("*") {
        override fun apply(x: Float, y: Float): MathTask {
            return SimpleMathTask(
                taskDescription = MathIntTaskDescription("$x ${this.sign} $y = ?"),
                answer = Answer(x * y),
                isGenerated = true
            )
        }
    }
}
