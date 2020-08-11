package uos.dev.restcli

import com.github.ajalt.mordant.TermColors
import com.jakewharton.picnic.table
import mu.KotlinLogging
import okhttp3.logging.HttpLoggingInterceptor
import picocli.CommandLine
import uos.dev.restcli.executor.OkhttpRequestExecutor
import uos.dev.restcli.jsbridge.JsClient
import uos.dev.restcli.parser.Parser
import uos.dev.restcli.parser.RequestEnvironmentInjector
import uos.dev.restcli.report.TestReportPrinter
import uos.dev.restcli.report.TestReportStore
import java.io.File
import java.io.FileReader
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "rest-cli", version = ["IntelliJ RestCli v1.3"],
    mixinStandardHelpOptions = true,
    description = ["@|bold IntelliJ RestCli|@"]
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
        description = ["Create test report inside folder \"test-reports\""]
    )
    var isCreateTestReport: Boolean = false

    private val t: TermColors = TermColors()
    private val logger = KotlinLogging.logger {}

    override fun call() {
        showInfo()

        val parser = Parser()
        val jsClient = JsClient()
        val requestEnvironmentInjector = RequestEnvironmentInjector()
        val environment = (environmentName?.let { EnvironmentLoader().load(it) } ?: emptyMap())
            .toMutableMap()

        val requests = parser.parse(FileReader(httpFilePath))

        val executor = OkhttpRequestExecutor(logLevel)
        TestReportStore.clear()
        requests.forEach { rawRequest ->
            runCatching {
                val jsGlobalEnv = jsClient.globalEnvironment()
                val request =
                    requestEnvironmentInjector.inject(rawRequest, environment, jsGlobalEnv)
                TestReportStore.addTestGroupReport(request.requestTarget)
                logger.info("\n__________________________________________________\n")
                logger.info(t.bold("##### ${request.method.name} ${request.requestTarget} #####"))
                runCatching { executor.execute(request) }
                    .onSuccess { response ->
                        jsClient.updateResponse(response)
                        request.scriptHandler?.let { script ->
                            val testTitle = t.bold("TESTS:")
                            logger.info("\n$testTitle")
                            jsClient.execute(script)
                        }
                    }
                    .onFailure {
                        val hasScriptHandler = request.scriptHandler != null
                        if (hasScriptHandler) {
                            logger.info(t.yellow("[SKIP TEST] Because: ") + it.message.orEmpty())
                        }
                    }
            }.onFailure { logger.error { t.red(it.message.orEmpty()) } }
        }
        logger.info("\n__________________________________________________\n")

        TestReportPrinter(
            testReportName = File(httpFilePath).nameWithoutExtension,
            isCreateTestReport = isCreateTestReport
        ).print(TestReportStore.testGroupReports)
    }

    private fun showInfo() {
        val content = table {
            style { border = true }
            header {
                cellStyle { border = true }
                row {
                    cell("restcli v1.3") {
                        columnSpan = 2
                    }
                }
                row("Environment name", environmentName)
            }
        }.toString()
        logger.info(content)
        logger.info("Test file: $httpFilePath")
    }
}
