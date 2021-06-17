package ru.tg.pawaptz.achievments

import com.vdurmont.emoji.EmojiManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FunnySmilesTest {

    @Test
    fun testPositiveSmiles() {
        FunnySmiles.values().forEach {
            assertThat(EmojiManager.getForAlias(it.alias)).describedAs(it.name).isNotNull
        }
    }
}