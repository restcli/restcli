package uos.dev.restcli.extension

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
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

fun String.glob(): List<String> {

    if( ! this.startsWith("glob:") ) return listOf(this)

    val pattern = this.removePrefix("glob:")

    var root = pattern
        .replaceFirst("^(.*?)([*?{\\[].*)$".toRegex(),"$1")
        .replace("\\","/")
        .substringBeforeLast("/")
        .also { if(it.isEmpty()) "." }

    var matcher = FileSystems.getDefault().getPathMatcher("glob:${pattern.removePrefix(root).removePrefix("/")}")

    return try {
        Files.walk(Paths.get(root))
            .filter{ it: Path? -> it?.let { matcher.matches(it.fileName) } ?: false }
            .collect(Collectors.toList())
            .map { it.toString() }
    } catch (e: Exception) {
        listOf(this)
    }

}