package com.mafazaa.ainaa.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.utils.MyLog

class ServiceMonitorJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        try {
            MyLog.i(TAG, "ServiceMonitorJobService started")

            // Check if accessibility service is enabled
            if (!isAccessibilityServiceEnabled(this)) {
                MyLog.w(TAG, "Accessibility service is not enabled")
            } else if (!MyAccessibilityService.isRunning) {
                MyLog.i(TAG, "Accessibility service enabled but not running, attempting to start")
                startAccessibilityService()
            } else {
                MyLog.d(TAG, "Accessibility service is running normally")
            }

            // Reschedule the job
            scheduleJob(this)

        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onStartJob: ${e.message}", e)
        }

        // Job is finished
        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return try {
            MyLog.i(TAG, "ServiceMonitorJobService stopped")
            true // Reschedule the job
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onStopJob: ${e.message}", e)
            true // Reschedule even on error
        }
    }

    companion object {
        private const val TAG = "ServiceMonitorJob"
        private const val JOB_ID = 1001

        fun scheduleJob(context: Context) {
            try {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

                // Check if job is already scheduled
                val pendingJob = jobScheduler.getPendingJob(JOB_ID)
                if (pendingJob != null) {
                    MyLog.d(TAG, "Job already scheduled")
                    return
                }

                val componentName = ComponentName(context, ServiceMonitorJobService::class.java)
                val jobInfo = JobInfo.Builder(JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .setPersisted(true) // Persist across reboots
                    .setPeriodic(15 * 60 * 1000) // Check every 15 minutes (minimum allowed)
                    .build()

                val result = jobScheduler.schedule(jobInfo)
                if (result == JobScheduler.RESULT_SUCCESS) {
                    MyLog.i(TAG, "Job scheduled successfully")
                } else {
                    MyLog.e(TAG, "Job scheduling failed")
                }
            } catch (e: Exception) {
                MyLog.e(TAG, "Error scheduling job: ${e.message}", e)
            }
        }

        fun cancelJob(context: Context) {
            try {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                jobScheduler.cancel(JOB_ID)
                MyLog.i(TAG, "Job cancelled")
            } catch (e: Exception) {
                MyLog.e(TAG, "Error cancelling job: ${e.message}", e)
            }
        }

        private fun isAccessibilityServiceEnabled(context: Context): Boolean {
            return try {
                val service = "${context.packageName}/${MyAccessibilityService::class.java.name}"
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                enabledServices?.contains(service) == true
            } catch (e: Exception) {
                MyLog.e(TAG, "Error checking if accessibility service is enabled: ${e.message}", e)
                false
            }
        }
    }
}

