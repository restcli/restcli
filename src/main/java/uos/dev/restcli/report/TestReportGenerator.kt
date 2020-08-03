package uos.dev.restcli.report

import java.io.Writer

interface TestReportGenerator {
    fun generate(testReports: List<TestReport>, writer: Writer)
}
