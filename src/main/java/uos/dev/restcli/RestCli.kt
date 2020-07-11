package uos.dev.restcli

import picocli.CommandLine
import java.util.concurrent.Callable

class RestCli : Callable<Unit> {
    @CommandLine.Option(
        names = ["-e", "--env"],
        description = ["Path to the environment config file."]
    )
    lateinit var environmentFilePath: String

    @CommandLine.Option(names = ["-s", "--script"], description = ["Path to the http script file."])
    lateinit var httpFilePath: String

    override fun call() {
        println("Env: $environmentFilePath; Script: $httpFilePath")
    }
}
