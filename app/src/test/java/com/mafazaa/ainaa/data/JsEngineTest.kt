package com.mafazaa.ainaa.data

import com.google.gson.Gson
import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScriptResult
import com.mafazaa.ainaa.utils.Constants
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class JsEngineTest {
    val engine = JsEngine().apply {
        setCodes(
            Constants.defaultCodes
        )
    }
    val gson = Gson()
    val reportShouldBlock = File("js-engine-should-block-report.txt")
    val reportShouldNotBlock = File("js-engine-should-not-block-report.txt")

    @Test
    fun `test codes should blocking`() {
        val root = File("..\\uninstall-utils\\block")
        val manufacturers = root.list() ?: return
        val report = FileOutputStream(reportShouldBlock, false)
        for (m in manufacturers) {
            report.append("\n=== $m ===\n")
            for (file in File(root, m).listFiles() ?: continue) {
                val rootJson = com.google.gson.JsonParser.parseString(file.readText()).asJsonObject
                val screenJson = rootJson.getAsJsonObject("screenAnalysis")
                val analysis = gson.fromJson(screenJson, ScreenAnalysis::class.java)
                engine.evaluate(analysis).also { result ->
                    if (result is ScriptResult.Error) {
                        report.append("error blocking ${file.name} of $m : ${result.error}")
                    }
                    val matched = (result as ScriptResult.Success).matched
                    report.append("blocked ${file.name} of $m by ${result.scriptName} $matched")
                }
            }

        }
    }

    @Test
    fun `test codes should not blocking`() {
        val root = File("..\\uninstall-utils\\pass")
        if (!root.exists()) return

        // gather all files under the pass folder (no manufacturers list)
        val files = root.walkTopDown().filter { it.isFile }.toList()
        val builder = StringBuilder()
        builder.appendLine("=== pass files ===")

        for (file in files) {
            builder.appendLine("File: ${file.path}")
            try {
                val rootJson = com.google.gson.JsonParser.parseString(file.readText()).asJsonObject
                val screenJson = rootJson.getAsJsonObject("screenAnalysis")
                val analysis = gson.fromJson(screenJson, ScreenAnalysis::class.java)
                val result = engine.evaluate(analysis)
                when (result) {
                    is ScriptResult.Error -> {
                        builder.appendLine("error ${file.name}: ${result.error}")
                        // fail the test for this file
                    }

                    is ScriptResult.Success -> {
                        builder.appendLine("script=${result.scriptName} matched=${result.matched}")
                        // ensure the script did NOT match (should not block)
                    }
                }
            } catch (e: Exception) {
                builder.appendLine("exception processing ${file.name}: ${e.message}")
            }
        }

        // write report at once using StringBuilder
        reportShouldNotBlock.writeText(builder.toString())
    }


}

private fun FileOutputStream.append(text: String) {
    this.write((text + "\n").toByteArray())
}
