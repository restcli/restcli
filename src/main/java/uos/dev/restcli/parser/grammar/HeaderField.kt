package uos.dev.restcli.parser.grammar

import uos.dev.restcli.Outcome

class HeaderField : Grammar<HeaderField.Data>() {
    data class Data(
        val input: String,
        val name: String,
        val value: String
    )

    override fun parse(input: String): Outcome<Data> {
        val matchResult = PATTERN.matchEntire(input)
            ?: return Outcome.Error(createParserException(input))
        val name = matchResult.groups[HEADER_NAME_GROUP_INDEX]?.value?.trim()
            ?: return Outcome.Error(createParserException(input))
        val value = matchResult.groups[HEADER_VALUE_GROUP_INDEX]?.value
            ?.trimStart()
            .orEmpty()
        return Outcome.Success(
            Data(
                input = input,
                name = name,
                value = value
            )
        )
    }

    companion object {
        /**
         * Each header field consists of a case-insensitive field name followed by a colon (‘:’), optional leading whitespace, the field value, and optional trailing whitespace.
         *
         * Header fields are send as is without encoding.
         * ```
         * headers:
         *       (header-field new-line)*
         * ```
         * ```
         * header-field:
         *       field-name ‘:’ optional-whitespace field-value optional-whitespace
         * ```
         * field-name:
         *       (any input-character except ‘:’)+
         * ```
         * ```
         * field-value:
         *       line-tail [new-line-with-indent field-value]
         * ```
         * Example:
         * ```
         * GET http://example.com/api/get?id=15
         * From: user@example.com
         * ```
         */
        val PATTERN: Regex = "([^\\s:]+):\\s+(.*)".toRegex()

        private const val HEADER_NAME_GROUP_INDEX = 1
        private const val HEADER_VALUE_GROUP_INDEX = 2
    }
}
