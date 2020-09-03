package uos.dev.restcli

import picocli.CommandLine
import kotlin.system.exitProcess

fun main(vararg args: String) {
    val exitCode = CommandLine(RestCli())
        .apply { isCaseInsensitiveEnumValuesAllowed = true }
        .execute(*args)
    exitProcess(exitCode)
}
