package com.github.ykrank.alipaybillaccessibility

import android.content.Context
import android.util.Log
import com.github.ykrank.androidtools.util.ErrorParser
import com.github.ykrank.androidtools.util.L

object ErrorUtil : ErrorParser {

    const val BUGLY_APP_ID = "eae39d8732"

    private val TAG_LOG = ErrorUtil::class.java.simpleName

    override fun parse(context: Context, throwable: Throwable): String {
        return throwable.localizedMessage
    }

    override fun throwNewErrorIfDebug(throwable: RuntimeException) {
        if (BuildConfig.DEBUG) {
            throw throwable
        } else {
            L.report(throwable, Log.WARN)
        }
    }
}
