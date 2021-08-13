package uos.dev.restcli.configs

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class StarConfigDecoratorTest {

    @Test
    @DisplayName("Star config decorator replace all chars by *")
    fun decorate() {
        assertThat(StarConfigDecorator.decorate("Test of secret")).isEqualTo("**************")
    }
}
