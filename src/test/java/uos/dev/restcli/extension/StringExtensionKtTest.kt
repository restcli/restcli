package uos.dev.restcli.extension

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class StringExtensionKtTest {

    @Test
    fun `glob test`() {
        Assertions.assertTrue( "glob:*.http".glob().size > 1 )
        Assertions.assertTrue( "glob:./*.http".glob().size > 1 )
        Assertions.assertTrue( "glob:../../*.http".glob().size > 1 )
        Assertions.assertTrue( "*.http".glob()[0] == "*.http" )
    }

}