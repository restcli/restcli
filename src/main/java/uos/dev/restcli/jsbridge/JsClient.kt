package uos.dev.restcli.jsbridge

import okhttp3.Response
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class JsClient {
    private val engine: ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")
    private val invocable: Invocable = engine as Invocable

    private val client: Any by lazy {
        engine.eval("client")
    }

    init {
        val reader = javaClass.classLoader.getResourceAsStream("client.js")?.reader()
        engine.eval(reader)
    }

    fun execute(testScript: String) {
        engine.eval(testScript)
    }

    // TODO: Make abstract from okhttp response.
    fun updateResponse(response: Response) {

    }
}
