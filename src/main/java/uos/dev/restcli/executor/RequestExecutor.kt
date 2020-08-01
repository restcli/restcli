package uos.dev.restcli.executor

import okhttp3.Response
import uos.dev.restcli.parser.Request

interface RequestExecutor {
    // TODO: Remove the deps from okhttp Response.
    fun execute(request: Request): Response
}
