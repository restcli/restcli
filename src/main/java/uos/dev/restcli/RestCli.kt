package uos.dev.restcli

import okhttp3.logging.HttpLoggingInterceptor
import picocli.CommandLine
import uos.dev.restcli.executor.OkhttpRequestExecutor
import uos.dev.restcli.jsbridge.JsClient
import uos.dev.restcli.parser.Parser
import java.io.FileReader
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "restcli", version = ["Intellij RestCli v1.1"],
    mixinStandardHelpOptions = true,
    description = ["@|bold Intellij Restcli|@"]
)
class RestCli : Callable<Unit> {
    @CommandLine.Option(
        names = ["-e", "--env"],
        description = [
            "Name of the environment in config file ",
            "(http-client.env.json/http-client.private.env.json)."
        ]
    )
    var environmentName: String? = null

    @CommandLine.Option(
        names = ["-s", "--script"],
        description = ["Path to the http script file."],
        required = true
    )
    lateinit var httpFilePath: String

    @CommandLine.Option(
        names = ["-l", "--log-level"],
        description = [
            "Config log level while the executor running. ",
            "Valid values: \${COMPLETION-CANDIDATES}"
        ]
    )
    var logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY

    override fun call() {
        println("Environment name: $environmentName; Script: $httpFilePath")
        val parser = Parser()
        val jsClient = JsClient()
        val environment = (environmentName?.let { EnvironmentLoader().load(it) } ?: emptyMap())
            .toMutableMap()
        val requests = parser.parse(FileReader(httpFilePath), environment)

        val executor = OkhttpRequestExecutor(logLevel)
        requests.forEach { request ->
            runSafe {
                log("\n__________________________________________________\n")
                log("##### Execute request ${request.requestTarget} #####")
                val response = executor.execute(request)
                jsClient.updateResponse(response)
                request.scriptHandler?.let { script ->
                    log(">>> Test script >>>")
                    jsClient.execute(script)
                    log("<<< Test script <<<")
                }
            }
        }
    }

    private fun runSafe(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            e.printStackTrace()
            log(e.message.orEmpty())
        }
    }

    private fun log(message: String) = println(message)
}
