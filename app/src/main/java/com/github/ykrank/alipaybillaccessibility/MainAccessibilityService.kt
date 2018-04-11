package com.github.ykrank.alipaybillaccessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import com.github.ykrank.androidtools.extension.toast
import com.github.ykrank.androidtools.util.L
import java.text.SimpleDateFormat
import java.util.*

class MainAccessibilityService : AccessibilityService() {
    private lateinit var prefManager: GeneralPreferences
    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefManager = App.get().prefManager
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
        if (event?.packageName == "com.miui.home") {
            if (!running) {
                if (System.currentTimeMillis() - prefManager.lastTime > 7 * 60_000 * 60 && (isInOnTime() || isInOffTime())) {
                    if (event.eventType == TYPE_WINDOW_CONTENT_CHANGED) {
                        L.d("Back to home")
                        running = true
                        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }, 300)
                    }
                }
            } else {
                if (event.eventType == TYPE_WINDOW_CONTENT_CHANGED) {
                    val targetNodes = rootInActiveWindow?.findAccessibilityNodeInfosByText("长江e家")
                    if (targetNodes?.isNotEmpty() == true) {
                        handler.postDelayed({ targetNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK) }, 500)
                    }
                }
            }
        } else if (event?.packageName == "com.cjsc.mobile") {
            if (event.className == "com.cjsc.mobile.activity.clockin.SignInActivity") {
                if (event.eventType == TYPE_WINDOW_STATE_CHANGED) {
                    L.d("签到页面")
                    val onTimeTitleNodes = rootInActiveWindow?.findAccessibilityNodeInfosByText("上班时间08:30")
                    if (onTimeTitleNodes?.isNotEmpty() == true) {
                        val contentNode = onTimeTitleNodes[0].parent
                        L.d(contentNode.toString())
                        for (x in 0 until contentNode.childCount) {
                            val node = contentNode.getChild(contentNode.childCount - 1 - x)
                            if (node.isVisibleToUser) {
                                val nodeText = node.text
                                if (!nodeText.isNullOrEmpty()) {
                                    if (nodeText.contains("打卡时间")) {
                                        val calendar = Calendar.getInstance()
                                        calendar.time = df1.parse(nodeText.substring(4))
                                        val today = Calendar.getInstance()
                                        today.time = Date()
                                        calendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE))
                                        updateLastTime(calendar.time.time)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateLastTime(time: Long) {
        prefManager.lastTime = time
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
            time = df.parse(if (BuildConfig.DEBUG) "23:59" else "20:00")
        }) && cNow.after(Calendar.getInstance().apply {
            time = df.parse("17:10")
        })
    }

    companion object {
        val df = SimpleDateFormat("HH:mm", Locale.CHINA)
        val df1 = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    }
}