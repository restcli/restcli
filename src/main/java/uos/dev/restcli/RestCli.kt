package uos.dev.restcli

import com.github.ajalt.mordant.TermColors
import com.jakewharton.picnic.table
import mu.KotlinLogging
import picocli.CommandLine
import picocli.CommandLine.Option
import uos.dev.restcli.extension.glob
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "rest-cli", version = ["IntelliJ RestCli v1.7.4"],
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

    @Option(
        names = ["-d", "--env-dir"],
        description = [
            "Directory where config files are (default: current directory)",
            "(http-client.env.json/http-client.private.env.json)."
        ]
    )
    var environmentFilesDirectory: String = ""

    @CommandLine.Parameters(
        paramLabel = "FILES",
        arity = "1..1000000",
        description = [
            "Path to one or more http script files.",
            "Glob pattens (ex. glob:/test/*.http)"
        ]
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

    @Option(
        names = ["-k", "--insecure"],
        description = ["Disable ssl validation"]
    )
    var insecure: Boolean = false

    @Option(
        names = ["-t", "--timeout"],
        description = ["Number of milliseconds for request timeout, default=3000"]
    )
    var requestTimeout: Long = 3000

    private val logger = KotlinLogging.logger {}

    override fun call(): Int {
        showInfo()
        val executor = HttpRequestFilesExecutor(
            httpFilePaths = httpFilePaths.flatMap{it.glob()}.toTypedArray(),
            environmentName = environmentName,
            customEnvironment = CustomEnvironment(privateEnv, publicEnv),
            logLevel = logLevel,
            environmentFilesDirectory = environmentFilesDirectory,
            insecure = insecure,
            requestTimeout = requestTimeout
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
                    cell(t.bold("restcli")) {
                        columnSpan = 2
                    }
                }
                row("Environment name", environmentName)
            }
        }.toString()
        logger.info(content)
    }
}