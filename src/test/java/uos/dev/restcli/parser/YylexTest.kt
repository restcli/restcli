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
        input: String,
        expectedType: Int,
        expectedValue: Any?
    ) {
        println("Input: $input")
        lexer.yyreset(input.toReader())
        val token = lexer.yylex()
        assertThat(token.type).isEqualTo(expectedType)
        assertThat(token.value).isEqualTo(expectedValue)
    }

    companion object {
        private fun String.toReader(): StringReader = StringReader(this)

        private fun createArgument(
            name: String,
            input: String,
            expectedType: Int,
            expectedValue: Any? = null
        ): Arguments = Arguments.of(name, input, expectedType, expectedValue)

        @JvmStatic
        private fun provideTestCases(): Stream<Arguments> = Stream.of(
            createArgument(
                name = "Request separator token with text.",
                input = "### The request separator.",
                expectedType = Yytoken.TYPE_SEPARATOR,
                expectedValue = "### The request separator."
            ),
            createArgument(
                name = "Request separator token.",
                input = "###",
                expectedType = Yytoken.TYPE_SEPARATOR,
                expectedValue = "###"
            )

        )
    }
}
