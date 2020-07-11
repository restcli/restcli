package uos.dev.restcli.parser.grammar

import uos.dev.restcli.Outcome

class OpenResponseHandler : Grammar<OpenResponseHandler.Data>() {
    data class Data(val input: String, val isScriptEmbedded: Boolean)

    override fun parse(input: String): Outcome<Data> {
        val matchResult = PATTERN.matchEntire(input)
            ?: return Outcome.Error(createParserException(input))
        val isScriptEmbedded = matchResult.groups[EMBED_SCRIPT_GROUP_INDEX] != null
        return Outcome.Success(Data(input, isScriptEmbedded))
    }

    companion object {
        private val PATTERN: Regex = ">\\s+((\\{%.*)|(.*))".toRegex()
        private const val EMBED_SCRIPT_GROUP_INDEX = 2
        private const val FILE_SCRIPT_GROUP_INDEX = 3
    }
}
