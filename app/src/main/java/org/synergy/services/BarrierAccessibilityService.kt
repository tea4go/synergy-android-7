package org.synergy.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent
import org.synergy.R

class BarrierAccessibilityService : AccessibilityService() {
    private lateinit var cursorView: View
    private lateinit var cursorLayout: LayoutParams
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()
        cursorView = View.inflate(baseContext, R.layout.cursor, null)
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LayoutParams.TYPE_PHONE;
        }
        cursorLayout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            layoutType,
            LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 200
            y = 200
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager.addView(cursorView, cursorLayout)
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(cursorView)
    }

    companion object {
        private const val TAG = "BarrierAccessibilitySer"
    }
}