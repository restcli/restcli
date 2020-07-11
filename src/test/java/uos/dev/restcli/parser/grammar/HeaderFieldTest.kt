package uos.dev.restcli.parser.grammar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.Outcome

class HeaderFieldTest {
    private val headerField = HeaderField()

    @Test
    fun parse_header_success() {
        val text = "Accept:       application/json"
        val result = headerField.parse(text)
        val data = (result as Outcome.Success).data
        assertThat(data.name).isEqualTo("Accept")
        assertThat(data.value).isEqualTo("application/json")
    }

    @Test
    fun parse_header_error() {
        val text = " Accept: application/json"
        val result = headerField.parse(text)
        assertThat(result).isInstanceOf(Outcome.Error::class.java)
    }
}

