package uos.dev.restcli.parser

import java.lang.Exception

class ParserException(val position: Int) : Exception("Error at $position")
