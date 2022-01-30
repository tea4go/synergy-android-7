package org.synergy.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PointF
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import org.synergy.R
import org.synergy.services.BarrierAccessibilityAction.*


class BarrierAccessibilityService : AccessibilityService() {
    private lateinit var cursorView: View
    private lateinit var cursorLayout: LayoutParams
    private lateinit var windowManager: WindowManager

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = BarrierAccessibilityAction.getActionFromIntent(intent) ?: return
            handleAction(action)
        }
    }

    override fun onCreate() {
        super.onCreate()
        cursorView = View.inflate(baseContext, R.layout.cursor, null).apply {
            visibility = View.GONE
        }
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            LayoutParams.TYPE_PHONE
        }
        cursorLayout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            layoutType,
            LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        registerBroadcastReceiver()
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

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter().apply {
            BarrierAccessibilityAction.getAllActions().forEach { addAction(it) }
        }
        registerReceiver(receiver, filter)
    }

    private fun handleAction(action: BarrierAccessibilityAction) {
        when (action) {
            is MouseEnter -> mouseEnter()
            is MouseLeave -> mouseLeave()
            is MouseMove -> mouseMove(action.x, action.y)
            is MouseClick -> mouseClick(action.x, action.y)
            is MouseLongClick -> mouseLongClick(action.x, action.y)
            is Drag -> drag(action.fromX, action.fromY, action.toX, action.toY)
        }
    }

    private fun mouseEnter() {
        cursorView.visibility = View.VISIBLE
        windowManager.updateViewLayout(cursorView, cursorLayout)
    }

    private fun mouseLeave() {
        cursorView.visibility = View.GONE
        windowManager.updateViewLayout(cursorView, cursorLayout)
    }

    private fun mouseMove(x: Int, y: Int) {
        cursorLayout.x = x
        cursorLayout.y = y
        windowManager.updateViewLayout(cursorView, cursorLayout)
    }

    private fun mouseClick(x: Int, y: Int) {
        Log.d(TAG, "mouseClick: $x, $y")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dispatchGesture(
                GestureDescription.Builder()
                    .addStroke(click(point(x - 1, y - 1)))
                    .build(),
                null,
                null,
            )
        }
    }

    private fun mouseLongClick(x: Int, y: Int) {
        Log.d(TAG, "mouseLongClick: $x, $y")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dispatchGesture(
                GestureDescription.Builder()
                    .addStroke(longClick(point(x - 1, y - 1)))
                    .build(),
                null,
                null,
            )
        }
    }

    private fun point(x: Int, y: Int) = PointF(x.toFloat(), y.toFloat())

    private fun drag(fromX: Int, fromY: Int, toX: Int, toY: Int) {
        // Log.d(TAG, "drag: $fromX, $fromY, $toX, $toY")
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //     val first = StrokeDescription(
        //         Path().apply { moveTo(fromX.toFloat() - 1, fromY.toFloat() - 1) },
        //         0,
        //         ViewConfiguration.getTapTimeout().toLong(),
        //         true,
        //     )
        //     val gesture = GestureDescription.Builder().apply {
        //         addStroke(first)
        //         addStroke(drag(first, PointF(toX.toFloat() - 1, toY.toFloat() - 1)))
        //     }.build()
        //     dispatchGesture(gesture, null, null)
        // }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun click(point: PointF) = StrokeDescription(
        path(point),
        0,
        1L,
    )

    @RequiresApi(Build.VERSION_CODES.N)
    fun longClick(point: PointF) = StrokeDescription(
        path(point),
        0,
        ViewConfiguration.getLongPressTimeout().toLong()
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drag(from: StrokeDescription, to: PointF) = from.continueStroke(
        path(lastPointOf(from), to),
        endTimeOf(from),
        ViewConfiguration.getTapTimeout().toLong(),
        true,
    )

    private fun path(first: PointF, vararg rest: PointF): Path {
        val path = Path()
        path.moveTo(first.x, first.y)
        for (point in rest) {
            path.lineTo(point.x, point.y)
        }
        return path
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun lastPointOf(stroke: StrokeDescription): PointF {
        val p = stroke.path.approximate(0.3f)
        return PointF(p[p.size - 2], p[p.size - 1])
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun endTimeOf(stroke: StrokeDescription): Long {
        return stroke.startTime + stroke.duration
    }

    companion object {
        private const val TAG = "BarrierAccessibilitySer"
    }
}