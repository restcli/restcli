package uos.dev.restcli

import uos.dev.restcli.configs.EnvironmentConfigs

/**
 * The data class represents the environment variables that user passed in the commandline.
 */
data class CustomEnvironment(
    val privateEnv: EnvironmentConfigs,
    val publicEnv: EnvironmentConfigs
)
