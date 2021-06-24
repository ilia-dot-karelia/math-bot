package ru.tg.pawaptz.chats.math.tasks.task

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MathIntTaskDescriptionTest {

    @Test
    fun whenIntegerTypesThenFirstDecimalPlaceMustBeRemoved() {
        assertThat(MathIntTaskDescription("12.3 + 12.0").question()).isEqualTo("12.3 + 12")
        assertThat(MathIntTaskDescription("12.0 + 12.0").question()).isEqualTo("12 + 12")
        assertThat(MathIntTaskDescription("0.0 + 1.0 = ?").question()).isEqualTo("0 + 1 = ?")
    }
}