package uos.dev.restcli

import com.github.ajalt.mordant.TermColors
import mu.KotlinLogging
import uos.dev.restcli.configs.DefaultMessageObfuscator
import uos.dev.restcli.configs.EnvironmentConfigs
import uos.dev.restcli.configs.MessageObfuscator
import uos.dev.restcli.configs.PrivateConfigDecorator
import uos.dev.restcli.executor.OkhttpRequestExecutor
import uos.dev.restcli.jsbridge.JsClient
import uos.dev.restcli.parser.Parser
import uos.dev.restcli.parser.Request
import uos.dev.restcli.parser.RequestEnvironmentInjector
import uos.dev.restcli.report.AsciiArtTestReportGenerator
import uos.dev.restcli.report.TestGroupReport
import uos.dev.restcli.report.TestReportPrinter
import uos.dev.restcli.report.TestReportStore
import java.io.PrintWriter

class HttpRequestFilesExecutor constructor(
    private val httpFilePaths: Array<String>,
    private val environmentName: String?,
    private val customEnvironment: CustomEnvironment,
    private val logLevel: HttpLoggingLevel,
    private val environmentFilesDirectory: String = "",
    private val insecure: Boolean,
    private val requestTimeout: Long,
    private val decorator: PrivateConfigDecorator
) : Runnable {
    private val parser: Parser = Parser()
    private val requestEnvironmentInjector: RequestEnvironmentInjector =
        RequestEnvironmentInjector()
    private val logger = KotlinLogging.logger {}
    private val t: TermColors = TermColors()

    override fun run() {
        if (httpFilePaths.isEmpty()) {
            logger.error { t.red("HTTP request file[s] is required") }
            return
        }
        val environment =
            environmentName?.let { EnvironmentLoader().load(environmentFilesDirectory, it) } ?: EnvironmentConfigs()
        val obfuscator = DefaultMessageObfuscator(environment, decorator)
        val executor = OkhttpRequestExecutor(
            logLevel.toOkHttpLoggingLevel(),
            obfuscator,
            insecure,
            requestTimeout,
        )
        val testGroupReports = mutableListOf<TestGroupReport>()
        TestReportStore.clear()
        httpFilePaths.forEach { httpFilePath ->
            logger.info("\n__________________________________________________\n")
            logger.info(t.bold("HTTP REQUEST FILE: $httpFilePath"))
            testGroupReports.clear()
            executeHttpRequestFile(
                httpFilePath,
                environment,
                executor,
                obfuscator
            )
            logger.info("\n__________________________________________________\n")

            TestReportPrinter(httpFilePath).print(TestReportStore.testGroupReports)
            testGroupReports.addAll(TestReportStore.testGroupReports)
        }
        val consoleWriter = PrintWriter(System.out)
        AsciiArtTestReportGenerator().generate(testGroupReports, consoleWriter)
        consoleWriter.flush()
    }

    fun allTestsFinishedWithSuccess(): Boolean {
        return TestReportStore.testGroupReports
            .flatMap { it.testReports }
            .all { it.isPassed }
    }

    private fun executeHttpRequestFile(
        httpFilePath: String,
        environment: EnvironmentConfigs,
        executor: OkhttpRequestExecutor,
        obfuscator: MessageObfuscator
    ) {
        val requests = try {
            parser.parse(httpFilePath)
        } catch (e: Exception) {
            logger.error(e) { "Can't parse $httpFilePath" }
            val trace = TestGroupReport.Trace(
                httpTestFilePath = httpFilePath,
                scriptHandlerStartLine = -1
            )
            TestReportStore.addTestGroupReport("-", trace)
            TestReportStore.addTestReport("Parsing", false, e.message, e.message)
            return
        }
        var requestIndex = -1
        while (requestIndex < requests.size) {
            val requestName = TestReportStore.nextRequestName
            TestReportStore.setNextRequest(null)
            if (requestName == REQUEST_NAME_END) {
                logger.warn { t.yellow("Next request is _END_ -> FINISH.") }
                return
            }
            if (requestName == null) {
                requestIndex++
            } else {
                val indexOfRequestName = requests.indexOfFirst { it.name == requestName }
                requestIndex = if (indexOfRequestName < 0) {
                    logger.warn {
                        t.yellow(
                            "Request name: $requestName is not defined yet." +
                                    " So continue execute the request by order"
                        )
                    }
                    requestIndex + 1
                } else {
                    indexOfRequestName
                }
            }

            executeSingleRequest(
                requests.getOrNull(requestIndex) ?: return,
                environment,
                httpFilePath,
                obfuscator,
                executor
            )

        }
    }

    private fun executeSingleRequest(
        rawRequest: Request,
        environment: EnvironmentConfigs,
        httpFilePath: String,
        obfuscator: MessageObfuscator,
        executor: OkhttpRequestExecutor
    ) {
        runCatching {
            val jsClient = JsClient()
            var jsGlobalEnv = EnvironmentConfigs.from(jsClient.globalEnvironment(), false)
            var request = requestEnvironmentInjector.inject(
                rawRequest,
                customEnvironment,
                environment,
                jsGlobalEnv
            )

            val trace = TestGroupReport.Trace(
                httpTestFilePath = httpFilePath,
                scriptHandlerStartLine = request.scriptHandlerStartLine
            )
            TestReportStore.addTestGroupReport(obfuscator.obfuscate(request.requestTarget), trace)
            logger.info("\n__________________________________________________\n")
            logger.info(t.bold("##### ${request.method.name} ${obfuscator.obfuscate(request.requestTarget)} #####"))
            request.scriptInit?.let<String, Unit> { script ->
                val testTitle = t.bold("PRE-REQUEST:")
                logger.info("\n$testTitle")
                runCatching {
                    jsClient.execute(script)
                    jsGlobalEnv = EnvironmentConfigs.from(jsClient.globalEnvironment(), false)
                    request = requestEnvironmentInjector.inject(
                        rawRequest,
                        customEnvironment,
                        environment,
                        jsGlobalEnv
                    )
                }.onFailure {
                    logger.error { t.red(it.message.orEmpty()) }
                    //TestReportStore.addTestReport("eval script", false, it.message, script)
                }
            }

            runCatching { executor.execute(request) }
                .onSuccess { response ->
                    jsClient.updateResponse(response)
                    request.scriptHandler?.let<String, Unit> { script ->
                        val testTitle = t.bold("TESTS:")
                        logger.info("\n$testTitle")
                        runCatching {
                            jsClient.execute(script)
                        }.onFailure {
                            logger.error { t.red(it.message.orEmpty()) }
                            TestReportStore.addTestReport("eval script", false, it.message, script)
                        }
                    }
                }
                .onFailure {
                    TestReportStore.addTestReport("Http", false, it.message, it.message)
                    val hasScriptHandler = request.scriptHandler != null
                    if (hasScriptHandler) {
                        logger.info(t.yellow("[SKIP TEST] Because: ") + it.message.orEmpty())
                    }
                }
            jsClient.globalEnvironment()
            jsClient.close()
        }.onFailure {
            logger.error { t.red(it.message.orEmpty()) }
            TestReportStore.addTestReport("-", false, it.message, it.message)
        }
    }

    companion object {
        /**
         * The specific request name. If the next request is sets to this name, the executor will
         * be end immediately.
         */
        const val REQUEST_NAME_END: String = "_END_"
    }
}
