package uos.dev.restcli.configs

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class NoopConfigDecoratorTest {

    @Test
    @DisplayName("Noop config decorator does nothing")
    fun decorate() {
        assertThat(NoopConfigDecorator.decorate("Test of secret")).isEqualTo("Test of secret")
    }
}
