package uos.dev.restcli.parser.grammar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.Outcome

class ResponseReferenceTest {

    @Test
    fun parse_success() {
        val responseHandler = ResponseReference()
        val result = responseHandler.parse("<> ./script.json")

        val data = (result as Outcome.Success).data
        assertThat(data.filePath).isEqualTo("./script.json")
    }
}

