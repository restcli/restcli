package uos.dev.restcli.executor

import uos.dev.restcli.parser.Request

interface RequestExecutor {
    fun execute(request: Request)
}
