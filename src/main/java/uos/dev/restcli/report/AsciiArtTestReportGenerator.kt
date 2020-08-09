package uos.dev.restcli.report

import com.jakewharton.picnic.Table
import com.jakewharton.picnic.table
import uos.dev.restcli.extension.autoWrap
import java.io.Writer

/**
 * Generate test report by using ascii art. The format looks like:
 * <pre>
 * ┌───────────────┐
 * │TEST RESULT    │
 * ├───────────────┤
 * │Total: 6       │
 * │Passed: 5      │
 * │Failed: 1      │
 * └───────────────┘
 * </pre>
 * This is especially useful when print to the console.
 */
class AsciiArtTestReportGenerator : TestReportGenerator {
    override fun generate(testGroupReports: List<TestGroupReport>, writer: Writer) {
        val allTestReports = testGroupReports.flatMap { it.testReports }
        val failedTestsCount = allTestReports.count { !it.isPassed }
        val passedTestsCount = allTestReports.size - failedTestsCount

        table {
            style { border = true }
            header {
                cellStyle { border = true }
                row("TEST RESULT")
            }
            body {
                row("Total: ${allTestReports.size}")
                row("Passed: $passedTestsCount")
                row("Failed: $failedTestsCount")
            }
        }.printlnTo(writer)

        if (failedTestsCount > 0) {
            table {
                style {
                    border = true
                }
                cellStyle {
                    border = true
                }
                header {
                    row {
                        cell("#")
                        cell("name")
                        cell("failure")
                        cell("detail")
                    }
                }
                body {
                    var index = 0
                    testGroupReports
                        .filter { it.testReports.any { report -> !report.isPassed } }
                        .forEach {
                            val failedTests = it.testReports.filter { report -> !report.isPassed }
                            row {
                                cell("[REQUEST] ${it.name}") {
                                    columnSpan = 4
                                }
                            }
                            failedTests.forEach { testReport ->
                                index += 1
                                row {
                                    cell(index)
                                    cell(testReport.name.autoWrap(30))
                                    cell(testReport.exception?.autoWrap(16))
                                    cell(testReport.detail.autoWrap(30))
                                }
                            }
                        }

                }
            }.printlnTo(writer)
        }
    }

    private fun Table.printlnTo(writer: Writer) {
        writer.write(toString() + "\n")
        writer.flush()
    }
}
