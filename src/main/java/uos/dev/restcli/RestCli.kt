package uos.dev.restcli

import com.github.ajalt.mordant.TermColors
import com.jakewharton.picnic.table
import mu.KotlinLogging
import picocli.CommandLine
import picocli.CommandLine.Option
import java.util.concurrent.Callable


@CommandLine.Command(
    name = "rest-cli", version = ["IntelliJ RestCli v1.6"],
    mixinStandardHelpOptions = true,
    description = ["@|bold IntelliJ RestCli|@"]
)
class RestCli : Callable<Int> {
    @Option(
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

    @Option(
        names = ["-l", "--log-level"],
        description = [
            "Config log level while the executor running. ",
            "Valid values: \${COMPLETION-CANDIDATES}"
        ]
    )
    var logLevel: HttpLoggingLevel = HttpLoggingLevel.BODY

    @Option(
        names = ["-P", "--private-env"],
        description = ["Private environment variables"]
    )
    var privateEnv: Map<String, String> = emptyMap()

    @Option(
        names = ["-G", "--global-env"],
        description = ["Public environment variables"]
    )
    var publicEnv: Map<String, String> = emptyMap()

    private val logger = KotlinLogging.logger {}
    private var exitCode = CommandLine.ExitCode.OK

    override fun call(): Int {
        showInfo()
        val executor = HttpRequestFilesExecutor(
            httpFilePaths = httpFilePaths,
            environmentName = environmentName,
            customEnvironment = CustomEnvironment(privateEnv, publicEnv),
            logLevel = logLevel
        )
        executor.run()
        return if (executor.allTestsFinishedWithSuccess()) {
            CommandLine.ExitCode.OK
        } else {
            CommandLine.ExitCode.SOFTWARE
        }
    }

    private fun showInfo() {
        val t = TermColors()
        val content = table {
            style { border = true }
            header {
                cellStyle { border = true }
                row {
                    cell(t.bold("restcli v1.6")) {
                        columnSpan = 2
                    }
                }
                row("Environment name", environmentName)
            }
        }.toString()
        logger.info(content)
    }
}
