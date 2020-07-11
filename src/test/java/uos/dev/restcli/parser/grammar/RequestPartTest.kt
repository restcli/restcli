package uos.dev.restcli.parser.grammar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.Outcome

class RequestPartTest {
    @Test
    fun parse_sucess() {
        val requestPart = RequestPart()
        val result = requestPart.parse("--my_boundary")
        val data = (result as Outcome.Success).data
        assertThat(data.boundary).isEqualTo("my_boundary")
    }
}
