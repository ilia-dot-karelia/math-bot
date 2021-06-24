package ru.tg.pawaptz.achievments

import com.vdurmont.emoji.EmojiManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class BasicSmilesTest {

    @Test
    fun assertAllAliasesAreValid() {
        SadSmiles.values().forEach {
            Assertions.assertThat(EmojiManager.getForAlias(it.alias)).describedAs(it.name)
                .isNotNull
        }
    }
}