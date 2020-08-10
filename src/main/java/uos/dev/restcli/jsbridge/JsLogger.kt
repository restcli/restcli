package uos.dev.restcli.jsbridge

import mu.KotlinLogging

object JsLogger {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun debug(message: String) = logger.debug { message }

    @JvmStatic
    fun info(message: String) = logger.info { message }

    @JvmStatic
    fun warn(message: String) = logger.warn { message }

    @JvmStatic
    fun error(message: String) = logger.error { message }

    @JvmStatic
    fun trace(message: String) = logger.trace { message }
}
