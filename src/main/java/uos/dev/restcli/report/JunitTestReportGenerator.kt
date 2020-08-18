package uos.dev.restcli.report

import org.apache.commons.text.StringEscapeUtils
import java.io.Writer

class JunitTestReportGenerator : TestReportGenerator {
    override fun generate(testGroupReports: List<TestGroupReport>, writer: Writer) {
        val totalTestsNumber = testGroupReports.map { it.testReports.size }.sum()
        val builder = StringBuilder()
            .append("""<?xml version="1.0" encoding="UTF-8"?>""")
            .append(NEW_LINE)
            .append("""<testsuites tests="$totalTestsNumber" name="IntelliJ Rest CLI - Test Generator">""")
            .append(NEW_LINE)
        testGroupReports
            .filter { it.testReports.isNotEmpty() }
            .forEach { groupReports ->
                val name = StringEscapeUtils.escapeXml11(groupReports.name)
                builder.append("""<testsuite tests="${groupReports.testReports.size}" name="$name">""")
                groupReports.testReports.forEach { report ->
                    builder.append(report.createTestCaseElement(groupReports.trace))
                        .append(NEW_LINE)
                }
                builder.append("</testsuite>")
            }
        builder.append("</testsuites>")
        writer.write(builder.toString())
    }

    private fun TestReport.createTestCaseElement(trace: TestGroupReport.Trace): String {
        val nameEscape = StringEscapeUtils.escapeXml11(name)
        val exceptionEscape = StringEscapeUtils.escapeXml11(exception ?: "TestScriptFailed")
        val detailWithTrace =
            "$detail\nFile: ${trace.httpTestFilePath}\nLine: ${trace.scriptHandlerStartLine}"
        val detailsEscape = StringEscapeUtils.escapeXml11(detailWithTrace)
        return if (isPassed) {
            """<testcase name="$nameEscape"/>"""
        } else {
            """
                <testcase name="$nameEscape">
                    <failure type="$exceptionEscape">$detailsEscape</failure>
                </testcase>
            """.trimIndent()
        }
    }

    companion object {
        private const val NEW_LINE = "\n"
    }
}
