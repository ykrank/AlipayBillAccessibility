package com.github.ykrank.alipaybillaccessibility

import android.content.res.Configuration
import android.os.Build
import android.os.StrictMode
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.github.ykrank.androidtools.AppDataProvider
import com.github.ykrank.androidtools.GlobalData
import com.github.ykrank.androidtools.extension.toast
import com.github.ykrank.androidtools.ui.UiDataProvider
import com.github.ykrank.androidtools.ui.UiGlobalData
import com.github.ykrank.androidtools.util.ErrorParser
import com.github.ykrank.androidtools.util.L
import com.github.ykrank.androidtools.util.LeaksUtil
import com.github.ykrank.androidtools.util.ProcessUtil
import com.github.ykrank.androidtools.widget.net.WifiActivityLifecycleCallbacks
import com.github.ykrank.androidtools.widget.track.DataTrackAgent
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class App : MultiDexApplication() {

    init {
        sApp = this
    }

    lateinit var refWatcher: RefWatcher
        private set

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        // enable StrictMode when debug
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

        GlobalData.init(object : AppDataProvider {
            override val errorParser: ErrorParser?
                get() = ErrorUtil
            override val logTag: String
                get() = LOG_TAG
            override val debug: Boolean
                get() = BuildConfig.DEBUG
            override val buildType: String
                get() = BuildConfig.BUILD_TYPE
            override val itemModelBRid: Int
                get() = BR.model
            override val recycleViewLoadingImgId: Int
                get() = R.drawable.loading
            override val recycleViewErrorImgId: Int
                get() = R.drawable.recycleview_error_symbol
            override val appR: Class<out Any>
                get() = R::class.java
        })
        refWatcher = LeaksUtil.install(this)
        L.init(this)

        UiGlobalData.init(object : UiDataProvider {
            override val refWatcher: RefWatcher?
                get() = this@App.refWatcher
            override val actLifeCallback: WifiActivityLifecycleCallbacks?
                get() = null
            override val trackAgent: DataTrackAgent?
                get() = null
        }, null, this::toast)

        L.l("App init")
        //如果不是主进程，不做多余的初始化
        if (!ProcessUtil.isMainProcess(this))
            return

        //enable vector drawable
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        L.l("App onConfigurationChanged")

        //如果不是主进程，不做多余的初始化
        if (!ProcessUtil.isMainProcess(this))
            return
    }

    companion object {
        val LOG_TAG = "S1NextLog"

        private lateinit var sApp: App

        fun get(): App {
            return sApp
        }
    }
}
