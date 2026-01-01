package com.mafazaa.ainaa.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.isServiceRunning
import com.mafazaa.ainaa.utils.startVpnService

/**
 * VpnMonitorJobService periodically checks if the VPN service is running
 * and restarts it if needed (when VPN permission is granted)
 */
class VpnMonitorJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        try {
            MyLog.i(TAG, "VpnMonitorJobService started")

            // Check if VPN permission is granted and service should be running
            if (hasVpnPermission()) {
                if (!isServiceRunning(this, MyVpnService::class.java)) {
                    MyLog.i(TAG, "VPN permission granted but service not running, attempting to start")
                    startVpnService()
                } else {
                    MyLog.d(TAG, "VPN service is running normally")
                }
            } else {
                MyLog.d(TAG, "VPN permission not granted, no action needed")
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
            MyLog.i(TAG, "VpnMonitorJobService stopped")
            true // Reschedule the job
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onStopJob: ${e.message}", e)
            true // Reschedule even on error
        }
    }

    companion object {
        private const val TAG = "VpnMonitorJob"
        private const val JOB_ID = 1002

        fun scheduleJob(context: Context) {
            try {
                val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler

                // Check if job is already scheduled
                val pendingJob = jobScheduler.getPendingJob(JOB_ID)
                if (pendingJob != null) {
                    MyLog.d(TAG, "VPN monitor job already scheduled")
                    return
                }

                val componentName = ComponentName(context, VpnMonitorJobService::class.java)
                val jobInfo = JobInfo.Builder(JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .setPersisted(true) // Persist across reboots
                    .setPeriodic(15 * 60 * 1000) // Check every 15 minutes (minimum allowed)
                    .build()

                val result = jobScheduler.schedule(jobInfo)
                if (result == JobScheduler.RESULT_SUCCESS) {
                    MyLog.i(TAG, "VPN monitor job scheduled successfully")
                } else {
                    MyLog.e(TAG, "VPN monitor job scheduling failed")
                }
            } catch (e: Exception) {
                MyLog.e(TAG, "Error scheduling VPN monitor job: ${e.message}", e)
            }
        }

        fun cancelJob(context: Context) {
            try {
                val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
                jobScheduler.cancel(JOB_ID)
                MyLog.i(TAG, "VPN monitor job cancelled")
            } catch (e: Exception) {
                MyLog.e(TAG, "Error cancelling VPN monitor job: ${e.message}", e)
            }
        }
    }
}

