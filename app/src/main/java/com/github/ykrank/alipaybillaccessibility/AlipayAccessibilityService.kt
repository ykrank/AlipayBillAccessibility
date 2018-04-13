package com.github.ykrank.alipaybillaccessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.support.v4.view.accessibility.AccessibilityEventCompat
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import com.github.ykrank.androidtools.extension.toast
import com.github.ykrank.androidtools.util.L
import com.github.ykrank.androidtools.util.RxJavaUtil
import com.google.android.accessibility.utils.WebInterfaceUtils
import com.google.android.accessibility.utils.compat.accessibilityservice.AccessibilityServiceCompatUtils
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AlipayAccessibilityService : AccessibilityService() {
    private lateinit var prefManager: GeneralPreferences

    private val billSubject = PublishSubject.create<AccessibilityEvent>()
    private val billDetailsSubject = PublishSubject.create<AccessibilityEvent>()
    private var billDisposable: Disposable? = null
    private var running = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefManager = App.get().prefManager
        toast(R.string.service_connected)

        //订阅账单内容改变的event
        billDisposable = billSubject
                .filter { running }
                .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .compose(RxJavaUtil.iOTransformer())
                .subscribe({
                    val rootChildCount = rootInActiveWindow.childCount
                    if (rootChildCount <= 4) {
                        return@subscribe
                    }
                    val listNode: AccessibilityNodeInfo? = rootInActiveWindow.getChild(4)?.getChild(0)
                    if (listNode != null) {
                        for (x in 0 until listNode.childCount) {
                            val node = listNode.getChild(x)
                            if (node?.className == "android.widget.LinearLayout" && node.childCount == 1) {
                                val node1 = node.getChild(0)
                                if (node1?.className == "android.widget.LinearLayout" && node1.childCount == 5) {
                                    val title = node1.getChild(0).text
                                    val balance = node1.getChild(1).text
                                    val kind = node1.getChild(2).text
                                    val day = node1.getChild(3).text //有可能是今天，昨天，具体日期
                                    val time = node1.getChild(4).text //17:55
                                }
                            }
                        }
                    } else {
                        toast("找不到List node")
                    }
                }, L::e)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        toast(R.string.service_unbind)
        RxJavaUtil.disposeIfNotNull(billDisposable)
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        L.d("onInterrupt")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            TYPE_WINDOW_STATE_CHANGED -> {
                if (event.className == "com.alipay.mobile.bill.list.ui.BillListActivity_") {
                    running = true
                    billSubject.onNext(event)
                } else if (event.className == "com.alipay.mobile.nebulacore.ui.H5Activity"
                        && event.text.contains("账单详情")) {
                    billDetailsSubject.onNext(event)
                }
            }
            TYPE_WINDOW_CONTENT_CHANGED -> {
                val rootInActiveWindow = AccessibilityServiceCompatUtils.getRootInActiveWindow(this)
                if (rootInActiveWindow?.childCount?:0 > 1) {
                    when (rootInActiveWindow.getChild(1).text) {
                        "账单" -> {
                            if (event.className == "android.widget.ListView") {
                                billSubject.onNext(event)
                            }
                        }
                        "账单详情" -> {
                            L.d("onAccessibilityEvent: $event")
                            var node1 = rootInActiveWindow.getChild(2)
                            if (!WebInterfaceUtils.isWebContainer(node1)){
                                node1 = node1.getChild(0)
                                if (!WebInterfaceUtils.isWebContainer(node1)){
                                    L.d("Not webcontainer")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    companion object {
        val df = SimpleDateFormat("HH:mm", Locale.CHINA)
        val df1 = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    }
}