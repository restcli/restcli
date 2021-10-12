package uos.dev.restcli.configs

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class MiddleStarConfigDecoratorTest {

    @Test
    @DisplayName("Middle star config decorator replace all chars by * except from X from start and end")
    fun decorate() {
        assertThat(MiddleStarConfigDecorator.decorate("Test of secret")).isEqualTo("T************t")
    }

    @Test
    @DisplayName("Middle star config decorator clea size can be adjusted")
    fun decorateWithCustomClearSize() {
        MiddleStarConfigDecorator.changeClearSize(3)
        assertThat(MiddleStarConfigDecorator.decorate("Test of secret")).isEqualTo("Tes********ret")
    }
}
