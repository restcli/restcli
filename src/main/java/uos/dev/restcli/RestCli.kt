package uos.dev.restcli

import picocli.CommandLine
import uos.dev.restcli.executor.OkhttpRequestExecutor
import uos.dev.restcli.parser.Parser
import java.io.FileReader
import java.util.concurrent.Callable

class RestCli : Callable<Unit> {
    @CommandLine.Option(
        names = ["-e", "--env"],
        description = ["Path to the environment config file."]
    )
    var environmentFilePath: String? = null

    @CommandLine.Option(
        names = ["-s", "--script"],
        description = ["Path to the http script file."],
        required = true
    )
    lateinit var httpFilePath: String

    override fun call() {
        println("Env: $environmentFilePath; Script: $httpFilePath")
        val parser = Parser()
        val executor = OkhttpRequestExecutor()
        val environmentLoader = EnvironmentLoader()

        val environment = environmentFilePath?.let { environmentLoader.load(it) } ?: emptyMap()
        val requests = parser.parse(FileReader(httpFilePath), environment)
        requests.forEach(executor::execute)
    }
}
