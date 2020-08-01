package uos.dev.restcli

import netscape.javascript.JSObject
import picocli.CommandLine
import uos.dev.restcli.executor.OkhttpRequestExecutor
import uos.dev.restcli.jsbridge.JsClient
import uos.dev.restcli.parser.Parser
import java.io.FileReader
import java.util.concurrent.Callable
import javax.script.Invocable
import javax.script.ScriptEngineManager

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
        println("Environment: $environmentFilePath; Script: $httpFilePath")
        val parser = Parser()
        val jsClient = JsClient()

        val environment = (environmentFilePath?.let { EnvironmentLoader().load(it) } ?: emptyMap())
            .toMutableMap()
        val requests = parser.parse(FileReader(httpFilePath), environment)

        val executor = OkhttpRequestExecutor()
        requests.forEach { request ->
            log("##### Execute request ${request.requestTarget} #####")
            val response = executor.execute(request)
            log(">>> Response >>>")
            log(response.body?.string().orEmpty())
            log("<<< Response <<<")
            request.scriptHandler?.let { script ->
                log(">>> Test script >>>")
                jsClient.execute(script)
                log("<<< Test script <<<")
            }
        }
    }

    private fun log(message: String) = println(message)
}
