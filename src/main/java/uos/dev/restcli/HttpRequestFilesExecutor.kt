package uos.dev.restcli

import com.github.ajalt.mordant.TermColors
import mu.KotlinLogging
import okhttp3.logging.HttpLoggingInterceptor
import uos.dev.restcli.executor.OkhttpRequestExecutor
import uos.dev.restcli.jsbridge.JsClient
import uos.dev.restcli.parser.Parser
import uos.dev.restcli.parser.Request
import uos.dev.restcli.parser.RequestEnvironmentInjector
import uos.dev.restcli.report.AsciiArtTestReportGenerator
import uos.dev.restcli.report.TestGroupReport
import uos.dev.restcli.report.TestReportPrinter
import uos.dev.restcli.report.TestReportStore
import java.io.File
import java.io.FileReader
import java.io.PrintWriter

class HttpRequestFilesExecutor constructor(
    private val httpFilePaths: Array<String>,
    private val environmentName: String?,
    private val logLevel: HttpLoggingInterceptor.Level,
    private val isCreateTestReport: Boolean
) : Runnable {
    private val parser: Parser = Parser()
    private val jsClient: JsClient = JsClient()
    private val requestEnvironmentInjector: RequestEnvironmentInjector =
        RequestEnvironmentInjector()
    private val logger = KotlinLogging.logger {}
    private val t: TermColors = TermColors()

    override fun run() {
        if (httpFilePaths.isEmpty()) {
            logger.error { t.red("HTTP request file[s] is required") }
            return
        }
        val environment = (environmentName?.let { EnvironmentLoader().load(it) } ?: emptyMap())
            .toMutableMap()
        val executor = OkhttpRequestExecutor(logLevel)
        val testGroupReports = mutableListOf<TestGroupReport>()
        httpFilePaths.forEach { httpFilePath ->
            logger.info(t.bold("Test file: $httpFilePath"))
            TestReportStore.clear()
            executeHttpRequestFile(
                httpFilePath,
                environment,
                executor
            )
            logger.info("\n__________________________________________________\n")

            if (isCreateTestReport) {
                TestReportPrinter(File(httpFilePath).nameWithoutExtension)
                    .print(TestReportStore.testGroupReports)
            }
            testGroupReports.addAll(TestReportStore.testGroupReports)
        }
        val consoleWriter = PrintWriter(System.out)
        AsciiArtTestReportGenerator().generate(testGroupReports, consoleWriter)
        consoleWriter.flush()
    }

    private fun executeHttpRequestFile(
        httpFilePath: String,
        environment: Map<String, String>,
        executor: OkhttpRequestExecutor
    ) {
        val requests = try {
            parser.parse(FileReader(httpFilePath))
        } catch (e: Exception) {
            logger.error(e) { "Can't parse $httpFilePath" }
            return
        }
        requests.forEach { rawRequest ->
            runCatching {
                val jsGlobalEnv = jsClient.globalEnvironment()
                val request =
                    requestEnvironmentInjector.inject(rawRequest, environment, jsGlobalEnv)
                TestReportStore.addTestGroupReport(request.requestTarget)
                logger.info("\n__________________________________________________\n")
                logger.info(t.bold("##### ${request.method.name} ${request.requestTarget} #####"))
                executeSingleRequest(executor, request)
            }.onFailure { logger.error { t.red(it.message.orEmpty()) } }
        }
    }

    private fun executeSingleRequest(executor: OkhttpRequestExecutor, request: Request) {
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
    }
}
