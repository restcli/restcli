package uos.dev.restcli.report

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class TestReportPrinter(
    private val testReportName: String,
    private val isCreateTestReport: Boolean = false
) {
    fun print(testGroupReports: List<TestGroupReport>) {
        val consoleWriter = PrintWriter(System.out)
        AsciiArtTestReportGenerator().generate(testGroupReports, consoleWriter)
        consoleWriter.flush()
        if (isCreateTestReport) {
            println("[START] Creating test report: $testReportName")
            generateJunitTestReport(testGroupReports)
            println("[FINISHED]")
        }
    }

    private fun generateJunitTestReport(testGroupReports: List<TestGroupReport>) {
        val result = runCatching {
            val reportName = testReportName.ifBlank { DEFAULT_TEST_REPORT_NAME }
            val reportDirectory = File(TEST_REPORTS_FOLDER_NAME).apply { mkdirs() }
            val reportFile = File(reportDirectory, "$reportName.xml")
            FileWriter(reportFile).use { JunitTestReportGenerator().generate(testGroupReports, it) }
        }
        result.onFailure {
            it.printStackTrace()
        }
    }

    companion object {
        private const val TEST_REPORTS_FOLDER_NAME = "test-reports"
        private const val DEFAULT_TEST_REPORT_NAME = "restcli-test-report"
    }
}
