package com.mafazaa.ainaa.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.mafazaa.ainaa.viewmodels.AppViewModel
import com.mafazaa.ainaa.BuildConfig
import com.mafazaa.ainaa.utils.Constants
import com.mafazaa.ainaa.data.JsEngine
import com.mafazaa.ainaa.data.local.RealFileRepo
import com.mafazaa.ainaa.data.UpdateManager
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.remote.FakeRemoteRepo
import com.mafazaa.ainaa.data.remote.KtorRepo
import com.mafazaa.ainaa.domain.FileRepo
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import com.mafazaa.ainaa.domain.repo.ScriptRepo
import com.mafazaa.ainaa.domain.repo.UpdateRepo
import com.mafazaa.ainaa.helpers.LockOverlayManager
import com.mafazaa.ainaa.helpers.ScreenshotOverlayManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<RemoteRepo> { if (BuildConfig.DEBUG) FakeRemoteRepo else KtorRepo() }
    single<SharedPrefs> { SharedPrefs(androidContext().getSharedPreferences("App", MODE_PRIVATE)) }
    single<FileRepo> { RealFileRepo(androidContext()) }
    single<LockOverlayManager> { LockOverlayManager(androidContext()) }
    single<ScreenshotOverlayManager> { ScreenshotOverlayManager(androidContext()) }
    single<ScriptRepo> {
        JsEngine().apply {
            setCodes(
                Constants.defaultCodes
            )
        }
    }
    single<UpdateRepo> { UpdateManager(get(), get(), get()) }

    viewModel { AppViewModel(get<Context>(),get(), get(), get(), get(), get()) }

}
