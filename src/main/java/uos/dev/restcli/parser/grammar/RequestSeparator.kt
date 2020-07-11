package uos.dev.restcli.parser.grammar

import uos.dev.restcli.Outcome

class RequestSeparator : Grammar<RequestSeparator.Data>() {
    data class Data(
        val input: String,
        val comment: String? = null
    )

    override fun parse(input: String): Outcome<Data> {
        val matchResult = PATTERN.matchEntire(input)
            ?: return Outcome.Error(createParserException(input))
        val comment = matchResult.groups[COMMENT_GROUP_INDEX]?.value?.trim()
        return Outcome.Success(Data(input, comment))
    }

    companion object {
        /**
         * Multiple requests defined in a single file must be separated from each other with a request separator symbol. A separator may contain comments.
         * ```
         * request-separator:
         *  ‘###’ line-tail
         * ```
         * __Example:__
         * ```
         * ###
         * ```
         * or
         * ```
         * ### request comment
         * ```
         */
        val PATTERN: Regex = "^###(.*)".toRegex()

        private const val COMMENT_GROUP_INDEX = 1
    }
}
