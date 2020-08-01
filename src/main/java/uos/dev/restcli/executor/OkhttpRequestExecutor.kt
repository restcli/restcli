package uos.dev.restcli.executor

import uos.dev.restcli.parser.Request

class OkhttpRequestExecutor : RequestExecutor {
    override fun execute(request: Request) {
        println("Execute: ${request.requestTarget}")
    }
}
