package uos.dev.restcli

import com.jakewharton.picnic.table
import mu.KotlinLogging
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "rest-cli", version = ["IntelliJ RestCli v1.4"],
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

    @CommandLine.Parameters(
        paramLabel = "FILES",
        arity = "1..1000000",
        description = ["Path to one ore more http script files."]
    )
    lateinit var httpFilePaths: Array<String>

    @CommandLine.Option(
        names = ["-l", "--log-level"],
        description = [
            "Config log level while the executor running. ",
            "Valid values: \${COMPLETION-CANDIDATES}"
        ]
    )
    var logLevel: HttpLoggingLevel = HttpLoggingLevel.BODY

    @CommandLine.Option(
        names = ["-r", "--report-names"],
        description = [
            "Custom test report names inside folder \"test-reports\".",
            "The report names must separate by ':' character;",
            "If the split string of the report name is empty, then the test request file name will",
            "be used for report name",
            "Such as:",
            "java -jar restcli.jar -r custom_report1::custom_report3 test1.http test2.http test3.http",
            "Then the test report for test1.http will be custom_report1.xml",
            "test2.http -> test2.xml (Because the report name for test2.http is empty)",
            "test3.http -> custom_report3.xml"
        ]
    )
    var testReportNames: String = ""

    private val logger = KotlinLogging.logger {}

    override fun call() {
        showInfo()
        HttpRequestFilesExecutor(
            httpFilePaths = httpFilePaths,
            environmentName = environmentName,
            logLevel = logLevel,
            testReportNames = testReportNames.split(':').toTypedArray()
        ).run()
    }

    private fun showInfo() {
        val content = table {
            style { border = true }
            header {
                cellStyle { border = true }
                row {
                    cell("restcli v1.4") {
                        columnSpan = 2
                    }
                }
                row("Environment name", environmentName)
            }
        }.toString()
        logger.info(content)
    }
}
