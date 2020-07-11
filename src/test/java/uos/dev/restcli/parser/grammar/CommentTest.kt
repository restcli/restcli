package uos.dev.restcli.parser.grammar

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.Outcome

class CommentTest {

    @Test
    fun parse_success() {
        val validComments = listOf(
            "## " to "#",
            "# Comment" to "Comment",
            "// Comment" to "Comment"
        )
        val commentGrammar = Comment()
        validComments.forEach { (input, expected) ->
            val result = commentGrammar.parse(input)
            val data = (result as Outcome.Success).data
            assertThat(data.comment).isEqualTo(expected)
        }
    }

    @Test
    fun parse_error() {
        val commentGrammar = Comment()
        val invalidComment = "Not A Comment"
        val result = commentGrammar.parse(invalidComment)
        assertThat(result).isInstanceOf(Outcome.Error::class.java)
    }
}
