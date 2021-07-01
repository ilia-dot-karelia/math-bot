package ru.tg.pawaptz.chats.math.tasks.task

import ru.tg.pawaptz.chats.math.tasks.gen.ComplexityAwareNumberGenerator
import kotlin.random.Random

enum class TaskComplexity : ComplexityAwareNumberGenerator {
    //todo support float
    EASY {
        override fun generate(): Float {
            return Random.Default.nextInt(11).toFloat()
        }
    },
    MIDDLE {
        override fun generate(): Float {
            return Random.Default.nextInt(11, 21).toFloat()
        }
    },
    ADVANCED {
        override fun generate(): Float {
            return Random.Default.nextInt(20, 101).toFloat()
        }
    },
    HARD {
        override fun generate(): Float {
            return Random.Default.nextInt(100, 10_000).toFloat()
        }
    };

    fun grow(): TaskComplexity {
        return when (this) {
            EASY -> MIDDLE
            MIDDLE -> ADVANCED
            ADVANCED -> HARD
            HARD -> HARD
        }
    }
}