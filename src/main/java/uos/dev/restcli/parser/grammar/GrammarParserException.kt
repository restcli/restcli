package uos.dev.restcli.parser.grammar

import java.lang.RuntimeException

class GrammarParserException(
    clazz: Class<*>,
    input: String
) : RuntimeException("Parse grammar ${clazz.simpleName} error for: $input")
