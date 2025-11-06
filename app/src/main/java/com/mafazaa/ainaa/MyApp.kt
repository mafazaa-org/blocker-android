package com.mafazaa.ainaa

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.work.Configuration
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.di.appModule
import com.mafazaa.ainaa.domain.FileRepo
import com.mafazaa.ainaa.receiver.BootReceiver
import com.mafazaa.ainaa.service.DailyUpdateNotificationWorker
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.isKeyguardSecure
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin
import java.util.concurrent.TimeUnit

class MyApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
            workManagerFactory()
        }

        val fileRepo: FileRepo = getKoin().get()
        MyLog.fileRepo = fileRepo // set the file repo for logging

        if (!isKeyguardSecure()) {//if the device is not encrypted
            val sharedPrefs: SharedPrefs = getKoin().get()
            if (sharedPrefs.lastVersion == 0) {//first run
                isFirstTime = true
                MyLog.i(TAG, "First run, initializing local data")
                sharedPrefs.lastVersion = BuildConfig.VERSION_CODE
            } else if (sharedPrefs.lastVersion < BuildConfig.VERSION_CODE) {// app updated
                MyLog.i(
                    TAG,
                    "App updated from version ${sharedPrefs.lastVersion} to ${BuildConfig.VERSION_CODE}"
                )
                sharedPrefs.lastVersion = BuildConfig.VERSION_CODE
                sharedPrefs.downloadedVersion = 0// reset downloaded version
            } else {
                MyLog.i(TAG, "App version is up to date: ${sharedPrefs.lastVersion}")
            }
        }
        registerBootReceiver()
        registerDailyUpdateNotification()

    }

    private fun registerDailyUpdateNotification() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()

        val dailyWorkRequest =
            PeriodicWorkRequestBuilder<DailyUpdateNotificationWorker>(
                1,
                TimeUnit.DAYS
            ) // minimum interval for testing
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "DailyNotificationWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
    }

    private fun registerBootReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BOOT_COMPLETED)
            addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED)
            addAction(Intent.ACTION_REBOOT)
        }
        ContextCompat.registerReceiver(
            this,
            BootReceiver(),
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(
                KoinWorkerFactory()
            )
            .build()

    companion object {
        private const val TAG = "MyApp"
        var isFirstTime = false
    }


}