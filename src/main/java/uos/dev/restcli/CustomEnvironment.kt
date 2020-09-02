package uos.dev.restcli

/**
 * The data class represents the environment variables that user passed in the commandline.
 */
data class CustomEnvironment(
    val privateEnv: Map<String, String>,
    val publicEnv: Map<String, String>
)
