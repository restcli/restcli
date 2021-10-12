package uos.dev.restcli.configs

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ThreeStarConfigDecoratorTest {

    @Test
    @DisplayName("Three star config decorator replace the value by ***")
    fun decorate() {
        assertThat(ThreeStarConfigDecorator.decorate("Test of secret")).isEqualTo("***")
    }
}
