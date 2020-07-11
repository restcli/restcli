package uos.dev.restcli.parser.grammar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.Outcome

class OpenResponseHandlerTest {

    @Test
    fun parse_success() {
        val responseHandler = OpenResponseHandler()
        val r1 = responseHandler.parse("> {% script; %}")
        val r2 = responseHandler.parse("> ./script.js")

        val data1 = (r1 as Outcome.Success).data
        val data2 = (r2 as Outcome.Success).data
        assertThat(data1.isScriptEmbedded).isTrue()
        assertThat(data2.isScriptEmbedded).isFalse()
    }
}

