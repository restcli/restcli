package uos.dev.restcli.parser.grammar

import uos.dev.restcli.Outcome

abstract class Grammar<T> {
    abstract fun parse(input: String): Outcome<T>

    companion object {
        fun <T : Grammar<*>> T.createParserException(input: String): GrammarParserException =
            GrammarParserException(javaClass, input)
    }
}
