package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.io.StringReader

class YylexTest {
    private val lexer = Yylex(null)

    @Test
    fun separator_token() {
        val input = "### The http request separator."
        lexer.yyreset(input.toReader())
        val token = lexer.yylex()
        assertThat(token.type).isEqualTo(Yytoken.TYPE_SEPARATOR)
    }

    companion object {
        private fun String.toReader(): StringReader = StringReader(this)
    }
}
