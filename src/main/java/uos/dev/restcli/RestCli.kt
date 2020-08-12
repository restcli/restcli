package uos.dev.restcli

import com.jakewharton.picnic.table
import mu.KotlinLogging
import picocli.CommandLine
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
        names = ["-r", "--report"],
        description = ["Create test report inside folder \"test-reports\""]
    )
    var isCreateTestReport: Boolean = false

    private val logger = KotlinLogging.logger {}

    override fun call() {
        showInfo()
        HttpRequestFilesExecutor(
            httpFilePaths = httpFilePaths,
            environmentName = environmentName,
            logLevel = logLevel,
            isCreateTestReport = isCreateTestReport
        ).run()
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
    }
}
