package uos.dev.restcli

import okhttp3.logging.HttpLoggingInterceptor
import picocli.CommandLine
import uos.dev.restcli.executor.OkhttpRequestExecutor
import uos.dev.restcli.jsbridge.JsClient
import uos.dev.restcli.parser.EnvironmentVariableInjector
import uos.dev.restcli.parser.EnvironmentVariableInjectorImpl
import uos.dev.restcli.parser.Parser
import uos.dev.restcli.parser.Request
import uos.dev.restcli.report.JunitTestReportGenerator
import uos.dev.restcli.report.TestReportGenerator
import uos.dev.restcli.report.TestReportStore
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "restcli", version = ["Intellij RestCli v1.2"],
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

    @CommandLine.Option(
        names = ["-r", "--report"],
        description = ["The name of the test report such as \"test_report\"."]
    )
    var testReportName: String? = null

    private val testReportGenerator: TestReportGenerator = JunitTestReportGenerator()
    private val environmentVariableInjector: EnvironmentVariableInjector =
        EnvironmentVariableInjectorImpl()

    override fun call() {
        println("Environment name: $environmentName; Script: $httpFilePath")
        val parser = Parser()
        val jsClient = JsClient()
        val environment = (environmentName?.let { EnvironmentLoader().load(it) } ?: emptyMap())
            .toMutableMap()

        val requests = parser.parse(FileReader(httpFilePath))

        val executor = OkhttpRequestExecutor(logLevel)
        TestReportStore.clear()
        requests.forEach { rawRequest ->
            runSafe {
                val jsGlobalEnv = jsClient.globalEnvironment()
                val request = injectEnv(rawRequest, environment, jsGlobalEnv)
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

        generateTestReport()
    }

    private fun injectEnv(
        request: Request,
        environment: Map<String, String>,
        jsGlobalEnv: Map<String, String>
    ): Request {
        fun inject(source: String): String =
            environmentVariableInjector.inject(source, jsGlobalEnv, environment)

        fun inject(headers: Map<String, String>): Map<String, String> {
            val result = mutableMapOf<String, String>()
            headers.forEach { (key, value) -> result[inject(key)] = inject(value) }
            return result
        }

        fun inject(part: Request.Part): Request.Part = part.copy(
            headers = inject(part.headers),
            body = part.body?.let(::inject)
        )

        return request.copy(
            requestTarget = inject(request.requestTarget),
            headers = inject(request.headers),
            body = request.body?.let(::inject),
            parts = request.parts.map(::inject)
        )
    }

    private fun generateTestReport() {
        val reportName = testReportName ?: return
        val format = SimpleDateFormat("yyyyMMddhhmm")
        val prefix = format.format(Date())
        val reportFile = File(".", "${prefix}_$reportName.xml")
        val writer = FileWriter(reportFile)
        writer.use { testReportGenerator.generate(TestReportStore.testReports, it) }
    }

    private fun runSafe(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            log(e.message.orEmpty())
            e.printStackTrace()
        }
    }

    private fun log(message: String) = println(message)
}
