package uos.dev.restcli.parser.grammar

import uos.dev.restcli.Outcome

class RequestPart : Grammar<RequestPart.Data>() {
    data class Data(val input: String, val boundary: String)

    override fun parse(input: String): Outcome<Data> {
        val matchResult = PATTERN.matchEntire(input)
            ?: return Outcome.Error(createParserException(input))
        val boundary = matchResult.groups[BOUNDARY_GROUP_INDEX]?.value.orEmpty()
        return Outcome.Success(Data(input, boundary))
    }

    companion object {
        private val PATTERN: Regex = "--(.*)".toRegex()
        private const val BOUNDARY_GROUP_INDEX = 1
    }
}
