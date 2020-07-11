package uos.dev.restcli.parser.grammar

import uos.dev.restcli.Outcome

class ResponseReference : Grammar<ResponseReference.Data>() {
    data class Data(val input: String, val filePath: String)

    override fun parse(input: String): Outcome<Data> {
        val matchResult = PATTERN.matchEntire(input)
            ?: return Outcome.Error(createParserException(input))
        val filePath = matchResult.groups[FILE_SCRIPT_GROUP_INDEX]?.value
            ?: return Outcome.Error(createParserException(input))
        return Outcome.Success(Data(input, filePath))
    }

    companion object {
        private val PATTERN: Regex = "<>\\s+(.*)".toRegex()
        private const val FILE_SCRIPT_GROUP_INDEX = 1
    }
}
