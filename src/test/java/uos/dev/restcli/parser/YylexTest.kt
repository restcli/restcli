package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.StringReader
import java.util.stream.Stream

class YylexTest {
    private val lexer = Yylex(null)

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestCases")
    fun generate_token(
        name: String,
        state: Int,
        input: String,
        expectedType: Int,
        expectedValue: Any?
    ) {
        println("Input: $input")
        lexer.yyreset(input.toReader())
        lexer.yybegin(state)
        val token = lexer.yylex()
        assertThat(token.type).isEqualTo(expectedType)
        assertThat(token.value).isEqualTo(expectedValue)
    }

    companion object {
        private fun String.toReader(): StringReader = StringReader(this)

        private fun createArgument(
            name: String,
            state: Int = Yylex.YYINITIAL,
            input: String,
            expectedType: Int,
            expectedValue: Any? = null
        ): Arguments = Arguments.of(name, state, input, expectedType, expectedValue)

        @JvmStatic
        private fun provideTestCases(): Stream<Arguments> = Stream.of(
            createArgument(
                name = "Request separator token with text.",
                input = "### The request separator.\n",
                expectedType = Yytoken.TYPE_SEPARATOR,
                expectedValue = "### The request separator."
            ),
            createArgument(
                name = "Request separator token.",
                input = "###\n",
                expectedType = Yytoken.TYPE_SEPARATOR,
                expectedValue = "###"
            ),
            createArgument(
                name = "Request line method.",
                input = "POST http://localhost.com",
                expectedType = Yytoken.TYPE_REQUEST_METHOD,
                expectedValue = "POST"
            ),
            createArgument(
                name = "Request line target.",
                input = "http://localhost.com",
                expectedType = Yytoken.TYPE_VALUE,
                expectedValue = "http://localhost.com"
            ),
            createArgument(
                name = "Header field.",
                input = "Content-Type: application/json\n",
                expectedType = Yytoken.TYPE_VALUE,
                expectedValue = "Content-Type: application/json",
                state = Yylex.S_HEADER
            )
        )
    }
}
