package com.mafazaa.ainaa.utils

import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import com.mafazaa.ainaa.BuildConfig
import com.mafazaa.ainaa.data.local.FakeFileRepo
import com.mafazaa.ainaa.domain.FileRepo
import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lg is a logging utility object that provides logging to both Logcat and a file.
 * The log file is wiped if it exceeds a size limit. It also provides utilities to log
 * UI tree analysis and package info to files for debugging or analysis purposes.
 */
object MyLog {
    /**
     * The repository used for file operations. Can be replaced for testing or production.
     */
    var fileRepo: FileRepo = FakeFileRepo // inject real one later

    /**
     * Maximum allowed log file size in bytes (10 MB).
     */
    const val LOG_FILE_SIZE_LIMIT = 1024 * 1024 * 10 // 10 MB

    /**
     * Writes a log message to the log file, including stack trace if provided.
     * Wipes the log file if it exceeds the size limit.
     *
     * @param level The log level (DEBUG, INFO, WARN, ERROR)//todo use or remove
     * @param tag The log tag
     * @param msg The log message
     * @param tr Optional throwable for stack trace
     */
    private fun logToFile(level: String, tag: String, msg: String, tr: Throwable? = null) {
        if (msg.isBlank()) return
        if (fileRepo.getLogSize() > LOG_FILE_SIZE_LIMIT) {
            fileRepo.wipeLog() // clear log if it exceeds size limit
        }
        val time = System.currentTimeMillis()
        val formatedTime = DateFormat.format("yyyy-MM-dd HH:mm:ss", time)
        val logLine = buildString {
            append("$formatedTime|$tag: $msg\n")
            if (tr != null) {
                append("-".repeat(15) + "\n")
                append(tr.stackTraceToString())
                append("-".repeat(15) + "\n")
            }
        }

        fileRepo.saveToLog(logLine)
    }


    fun d(tag: String, msg: String, tr: Throwable? = null): Int {
        val res = if (tr != null) Log.d(tag, msg, tr) else Log.d(tag, msg)
        logToFile("DEBUG", tag, msg, tr)
        return res
    }

    fun i(tag: String, msg: String, tr: Throwable? = null): Int {
        val res = if (tr != null) Log.i(tag, msg, tr) else Log.i(tag, msg)
        logToFile("INFO", tag, msg, tr)
        return res
    }


    fun w(tag: String, msg: String, tr: Throwable? = null): Int {
        val res = if (tr != null) Log.w(tag, msg, tr) else Log.w(tag, msg)
        logToFile("WARN", tag, msg, tr)
        return res
    }

    fun e(tag: String, msg: String, tr: Throwable? = null): Int {
        val res = if (tr != null) Log.e(tag, msg, tr) else Log.e(tag, msg)
        logToFile("ERROR", tag, msg, tr)
        return res
    }

    /**
     * Logs the UI tree analysis to a file named after the codeName.
     * Wipes the file before writing.
     * @param codeName The code name for the file
     * @param screenAnalysis The screen analysis data
     * @return The file where the log was written
     */
    fun logUiTree(codeName: String, screenAnalysis: ScreenAnalysis): File {
        val fileName = "$codeName.txt"
        val logFile = fileRepo.getLogFile(fileName)
        fileRepo.wipeLog(fileName)
        val jsonFileName = "$codeName.json"
        fileRepo.wipeLog(jsonFileName)
        val jsonPayload = buildString {
            append("{")
            append("\"meta\":{")
            append("\"versionName\":\"${BuildConfig.VERSION_NAME}\",")
            append("\"versionCode\":${BuildConfig.VERSION_CODE},")
            append("\"manufacturer\":\"${Build.MANUFACTURER}\",")
            append("\"model\":\"${Build.MODEL}\"},")
            append(
                "\"time\":\"${
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                        .format(Date(System.currentTimeMillis()))
                }\""
            )
            append("},")
            append("\"screenAnalysis\":")
            append(screenAnalysis.toJson())
            append("}")
        }
        fileRepo.saveToLog(jsonPayload, jsonFileName)
        return logFile
    }

    /**
     * Logs information about a falsely blocked app to a file named after the package name.
     */
    fun logFalseBlockedApp(packageName: String): File {
        val fileName = "$packageName.txt"
        val logFile = fileRepo.getLogFile(fileName)
        fileRepo.wipeLog(fileName)
        val info = buildString {
            append("Package: $packageName\n")
            append("Log file size: ${fileRepo.getLogSize()} bytes\n")
            append(
                "Time: ${
                    DateFormat.format(
                        "yyyy-MM-dd HH:mm:ss",
                        System.currentTimeMillis()
                    )
                }\n"
            )
        }
        fileRepo.saveToLog(info, fileName)
        return logFile
    }
}