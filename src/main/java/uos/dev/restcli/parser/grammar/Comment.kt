package uos.dev.restcli.parser.grammar

import uos.dev.restcli.Outcome

class Comment : Grammar<Comment.Data>() {
    data class Data(
        val input: String,
        val comment: String
    )

    override fun parse(input: String): Outcome<Data> {
        val matchResult = PATTERN.matchEntire(input)
            ?: return Outcome.Error(createParserException(input))
        val rawComment = matchResult.groups[COMMENT_TYPE_1_GROUP_INDEX]
            ?: matchResult.groups[COMMENT_TYPE_2_GROUP_INDEX]
            ?: return Outcome.Error(createParserException(input))
        return Outcome.Success(Data(input, rawComment.value.trim()))
    }

    companion object {
        /**
         * Line comments are supported in HTTP Requests. Comments can be used before or after a request, inside the header section, or within the request body. Comments used within the request body must start from the beginning of the line with or without indent.
         * ```
         * line-comment:
         *    ‘#’ line-tail
         *    ‘//’ line-tail
         * ```
         * Example:
         * ```
         *  # request comment
         *  // request comment
         * ```
         */
        private val PATTERN: Regex = "(^#(.*)|^//(.*))".toRegex()

        private const val COMMENT_TYPE_1_GROUP_INDEX = 2
        private const val COMMENT_TYPE_2_GROUP_INDEX = 3
    }
}
