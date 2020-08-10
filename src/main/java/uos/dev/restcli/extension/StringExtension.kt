package uos.dev.restcli.extension

import kotlin.math.min

/**
 * Auto break line of this string with `\n` character to ensure that the max characters per line is
 * not large than [maxCharactersPerLine].
 * For example:
 * <pre>
 *     "123456".autoWrap(3) => "123\n456"
 * </pre>
 */
fun String.autoWrap(maxCharactersPerLine: Int): String {
    val builder = StringBuilder()
    for (start in 0..length step maxCharactersPerLine) {
        val text = substring(start, min(length, start + maxCharactersPerLine))
        builder.append(text)
        if (start + maxCharactersPerLine < length) {
            builder.append("\n")
        }
    }
    return builder.toString()
}
