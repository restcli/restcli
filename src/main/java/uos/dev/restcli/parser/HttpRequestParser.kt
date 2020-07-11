package uos.dev.restcli.parser

import java.io.Reader

class HttpRequestParser {
    private val lexer: Yylex = Yylex(null)
    private var token: Yytoken? = null

    fun reset(input: Reader) {
        lexer.yyreset(input)
        reset()
    }

    private fun reset() {
        token = null
    }
}
