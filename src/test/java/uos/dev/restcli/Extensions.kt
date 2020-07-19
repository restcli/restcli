package uos.dev.restcli

import java.io.File
import java.io.InputStreamReader
import java.io.Reader

object TestResourceLoader {
    fun testResourceReader(path: String): Reader {
        val inputStream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw NullPointerException("Can't read input stream for $path")
        return InputStreamReader(inputStream)
    }

    fun testResourcePath(path: String): String {
        val fileUrl = javaClass.classLoader.getResource("requests/get-requests.http")?.file
            ?: throw NullPointerException("Can't read input for $path")
        return File(fileUrl).absolutePath
    }
}
