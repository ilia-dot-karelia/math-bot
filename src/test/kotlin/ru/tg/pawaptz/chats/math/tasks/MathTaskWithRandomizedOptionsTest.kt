package ru.tg.pawaptz.chats.math.tasks

import org.junit.jupiter.api.Test
import ru.tg.pawaptz.chats.math.tasks.task.MathTaskWithRandomizedOptions

internal class MathTaskWithRandomizedOptionsTest {

    @Test
    fun assertOptioningWork() {
        val rez = MathTaskWithRandomizedOptions(Operator.PLUS.apply(1f, 3f))
        print(rez)
    }
}