package uos.dev.restcli.parser.grammar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.Outcome

class RequestSeparatorTest {
    @Test
    fun parse_success() {
        val validSeparator = "### Get request"
        val requestSeparator = RequestSeparator()
        val result = requestSeparator.parse(validSeparator)
        val data = (result as Outcome.Success).data
        assertThat(data.comment).isEqualTo("Get request")
    }

    @Test
    fun parse_error() {
        val requestSeparator = RequestSeparator()
        val invalidSeparator = "## Not a request separator"
        val result = requestSeparator.parse(invalidSeparator)
        assertThat(result).isInstanceOf(Outcome.Error::class.java)
    }
}
