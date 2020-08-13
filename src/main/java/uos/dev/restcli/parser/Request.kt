package uos.dev.restcli.parser

data class Request(
    val name: String? = null,
    val method: RequestMethod = RequestMethod.GET,
    val requestTarget: String,
    val httpVersion: String = DEFAULT_HTTP_VERSION,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val scriptHandler: String? = null,
    val responseReference: String? = null,
    val parts: List<Part> = mutableListOf(),
    val isFollowRedirects: Boolean = false,
    val isNoCookieJar: Boolean = false,
    val isNoLog: Boolean = false,
    val isUseOsCredentials: Boolean = false
) {
    data class Part(
        val name: String,
        val headers: Map<String, String>,
        val body: String? = null,
        val fileName: String? = null
    ) {
        data class Builder @JvmOverloads constructor(
            var name: String = "",
            var fileName: String? = null,
            val headers: MutableMap<String, String> = mutableMapOf(),
            val rawBody: MutableList<String> = mutableListOf()
        ) {
            fun build(): Part = Part(
                name = name,
                fileName = fileName,
                headers = headers,
                body = rawBody.joinToStringAndRemoveBarrier()
            )
        }
    }

    data class Builder @JvmOverloads constructor(
        var name: String? = null,
        var method: RequestMethod = RequestMethod.GET,
        var requestTarget: String? = null,
        var httpVersion: String = DEFAULT_HTTP_VERSION,
        val headers: MutableMap<String, String> = mutableMapOf(),
        val rawBody: MutableList<String> = mutableListOf(),
        val rawScriptHandler: MutableList<String> = mutableListOf(),
        val rawResponseHandler: MutableList<String> = mutableListOf(),
        var rawResponseReference: String? = null,
        val parts: MutableList<Part.Builder> = mutableListOf(),
        var isFollowRedirects: Boolean = false,
        var isNoCookieJar: Boolean = false,
        var isNoLog: Boolean = false,
        var isUseOsCredentials: Boolean = false
    ) {
        private val isValid: Boolean
            get() = requestTarget?.isNotBlank() ?: false

        fun build(): Request? {
            if (!isValid) {
                return null
            }
            return Request(
                name = name,
                method = method,
                requestTarget = requestTarget.orEmpty(),
                httpVersion = httpVersion,
                headers = headers,
                body = rawBody.joinToStringAndRemoveBarrier(),
                scriptHandler = rawScriptHandler.joinToStringAndRemoveBarrier(),
                responseReference = rawResponseReference?.trim(),
                parts = parts.map { it.build() },
                isFollowRedirects = isFollowRedirects,
                isNoCookieJar = isNoCookieJar,
                isNoLog = isNoLog,
                isUseOsCredentials = isUseOsCredentials
            )
        }
    }

    companion object {
        const val DEFAULT_HTTP_VERSION: String = "HTTP/1.1"

        /**
         * A barrier string which used to wrap the content in the referenced file to ensure those
         * content will not be trimmed.
         * Notes that the barrier content will be removed when request is built.
         * For simple solution, we used a random uuid as barrier, so no-one can know that string
         * => the content will be keep safer.
         */
        private val BARRIER: String = java.util.UUID.randomUUID().toString()

        fun wrapContentWithBarrier(content: String): String = "$BARRIER$content$BARRIER"

        private fun removeBarrierFromContent(content: String): String = content.replace(BARRIER, "")

        private fun List<String>.joinToStringAndRemoveBarrier(): String? =
            joinToString("")
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.let(::removeBarrierFromContent)
    }
}
