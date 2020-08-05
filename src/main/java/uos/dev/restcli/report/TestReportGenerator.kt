package uos.dev.restcli.report

import java.io.Writer

interface TestReportGenerator {
    fun generate(testGroupReports: List<TestGroupReport>, writer: Writer)
}
