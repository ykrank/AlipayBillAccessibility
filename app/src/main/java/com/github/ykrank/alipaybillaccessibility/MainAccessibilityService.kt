package com.github.ykrank.alipaybillaccessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import com.github.ykrank.androidtools.extension.toast
import com.github.ykrank.androidtools.util.L
import java.text.SimpleDateFormat
import java.util.*

class MainAccessibilityService : AccessibilityService() {
    private var lastTime: Long = 0L
    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        toast(R.string.service_connected)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        toast(R.string.service_unbind)
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        L.d("onInterrupt")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        L.d("onAccessibilityEvent: $event")
        if (!running) {
            if (System.currentTimeMillis() - lastTime > 60_000 * 60 && (isInOnTime() || isInOffTime())) {
                if (event?.eventType == TYPE_WINDOW_CONTENT_CHANGED) {
                    L.d("Back to home")
                    running = true
                    handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }, 300)
                }
            }
        } else {
            if (event?.eventType == TYPE_WINDOW_CONTENT_CHANGED) {
                val targetNodes = rootInActiveWindow.findAccessibilityNodeInfosByText("长江e家")
                if (targetNodes.isNotEmpty()) {
                    handler.postDelayed({ targetNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK) }, 500)

                    updateLastTime()
                }
            }
        }
    }

    private fun updateLastTime() {
        lastTime = System.currentTimeMillis()
        running = false
    }

    private fun isInOnTime(): Boolean {
        val cNow = Calendar.getInstance().apply {
            time = df.parse(df.format(Date()))
        }
        return cNow.before(Calendar.getInstance().apply {
            time = df.parse("08:30")
        }) && cNow.after(Calendar.getInstance().apply {
            time = df.parse("07:50")
        })
    }

    private fun isInOffTime(): Boolean {
        val cNow = Calendar.getInstance().apply {
            time = df.parse(df.format(Date()))
        }
        return cNow.before(Calendar.getInstance().apply {
            time = df.parse("20:00")
        }) && cNow.after(Calendar.getInstance().apply {
            time = df.parse("17:10")
        })
    }

    companion object {
        val df = SimpleDateFormat("HH:mm", Locale.CHINA)
    }
}