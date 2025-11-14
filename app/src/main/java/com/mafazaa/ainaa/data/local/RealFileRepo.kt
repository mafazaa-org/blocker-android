package com.mafazaa.ainaa.data.local

import android.content.Context
import com.mafazaa.ainaa.domain.FileRepo
import java.io.File

class RealFileRepo(val context: Context) : FileRepo {

    override fun getLogFile(fileName: String): File = File(context.cacheDir, fileName)
    override fun getLogSize(fileName: String): Long {
        return getLogFile(fileName).length()
    }

    override fun wipeLog(fileName: String) {
        val logFile = getLogFile(fileName)
        if (logFile.exists()) {
            logFile.writeText("") // Clear the log file
        } else {
            logFile.createNewFile() // Create a new file if it doesn't exist
        }
    }

    override fun saveToLog(content: String, fileName: String) {
        val logFile = getLogFile(fileName)
        logFile.appendText(content, Charsets.UTF_8)
    }

    override fun getUpdateFile(): File = File(
        context.filesDir,
        "update.apk"
    )
}