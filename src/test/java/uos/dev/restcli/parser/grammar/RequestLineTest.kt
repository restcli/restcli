package uos.dev.restcli.parser.grammar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.Outcome
import uos.dev.restcli.parser.RequestMethod


class RequestLineTest {
    private val requestLine: RequestLine = RequestLine()

    @Test
    fun parse_full_request_success() {
        val text = "GET http://abc.xyz HTTP/1.1"
        val result = requestLine.parse(text)
        val data = (result as Outcome.Success).data
        assertThat(data.target).isEqualTo("http://abc.xyz")
        assertThat(data.method).isEqualTo(RequestMethod.GET)
        assertThat(data.httpVersion).isEqualTo("HTTP/1.1")
    }

    @Test
    fun parse_request_without_http_version_success() {
        val text = "GET http://abc.xyz"
        val result = requestLine.parse(text)
        val data = (result as Outcome.Success).data
        assertThat(data.target).isEqualTo("http://abc.xyz")
        assertThat(data.method).isEqualTo(RequestMethod.GET)
    }

    @Test
    fun parse_target_only_request_success() {
        val text = "http://abc.xyz"
        val result = requestLine.parse(text)
        val data = (result as Outcome.Success).data
        assertThat(data.target).isEqualTo("http://abc.xyz")
        assertThat(data.method).isEqualTo(RequestMethod.GET)
        assertThat(data.httpVersion).isNull()
    }
}
