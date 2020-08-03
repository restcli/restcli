package uos.dev.restcli.report

import org.apache.commons.text.StringEscapeUtils
import java.io.Writer

class JunitTestReportGenerator : TestReportGenerator {
    override fun generate(testReports: List<TestReport>, writer: Writer) {
        val builder = StringBuilder()
            .append("""<?xml version="1.0" encoding="UTF-8"?>""")
            .append(NEW_LINE)
            .append("""<testsuite tests="${testReports.size}" name="IntelliJ Rest CLI - Test Generator">""")
        testReports.forEachIndexed { index, report ->
            builder.append(report.createTestCaseElement(index + 1)).append(NEW_LINE)
        }
        builder.append("</testsuite>")
        writer.write(builder.toString())
    }

    private fun TestReport.createTestCaseElement(id: Int): String {
        val nameEscape = StringEscapeUtils.escapeXml11("[$id] $name")
        val detailsEscape = StringEscapeUtils.escapeXml11(details)
        return if (isPassed) {
            """<testcase classname="IntelliJRestCliTest" name="$nameEscape"/>"""
        } else {
            """
                <testcase classname="IntelliJRestCliTest" name="$nameEscape">
                    <failure type="TestScriptFailed">$detailsEscape</failure>
                </testcase>
            """.trimIndent()
        }
    }

    companion object {
        private const val NEW_LINE = "\n"
    }
}
