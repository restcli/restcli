package uos.dev.restcli

import picocli.CommandLine
import kotlin.system.exitProcess

fun main(vararg args: String) {
    val exitCode = CommandLine(RestCli())
        .apply { setCaseInsensitiveEnumValuesAllowed(true) }
        .execute(*args)
    exitProcess(exitCode)
}
