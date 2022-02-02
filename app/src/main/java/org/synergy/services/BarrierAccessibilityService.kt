package org.synergy.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent.KEYCODE_ESCAPE
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.FOCUS_INPUT
import org.synergy.R
import org.synergy.common.key.Key
import org.synergy.services.BarrierAccessibilityAction.*
import org.synergy.utils.AccessibilityNodeInfoUtils.handleNonCharKey
import org.synergy.utils.AccessibilityNodeInfoUtils.insertText
import org.synergy.utils.GestureUtils.click
import org.synergy.utils.GestureUtils.longClick
import org.synergy.utils.GestureUtils.path


class BarrierAccessibilityService : AccessibilityService() {
    private lateinit var cursorView: View
    private lateinit var cursorLayout: LayoutParams
    private lateinit var windowManager: WindowManager

    private val root: AccessibilityNodeInfoCompat
        get() = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)

    private var focusedInputNode: AccessibilityNodeInfoCompat? = null

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

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Log.d(TAG, "onAccessibilityEvent: $event")
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            return
        }
        focusedInputNode?.recycle() // recycle previous node
        focusedInputNode = root.findFocus(FOCUS_INPUT)
    }

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
            is Drag -> drag(action.dragPoints, action.duration)
            is KeyDown -> keyDown(action.key)
            is KeyUp -> keyUp(action.key)
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
        // Log.d(TAG, "mouseClick: $x, $y")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dispatchGesture(
                GestureDescription.Builder()
                    .addStroke(click(Point(x - 1, y - 1)))
                    .build(),
                null,
                null,
            )
        }
    }

    private fun mouseLongClick(x: Int, y: Int) {
        // Log.d(TAG, "mouseLongClick: $x, $y")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dispatchGesture(
                GestureDescription.Builder()
                    .addStroke(longClick(Point(x - 1, y - 1)))
                    .build(),
                null,
                null,
            )
        }
    }

    private fun drag(dragPoints: List<Point>, duration: Long) {
        // Log.d(TAG, "drag: $dragPoints")
        if (dragPoints.isEmpty()) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val stroke = StrokeDescription(
                path(dragPoints),
                0,
                duration,
            )
            val gesture = GestureDescription.Builder()
                .addStroke(stroke)
                .build()
            dispatchGesture(gesture, null, null)
        }
    }

    private fun keyDown(key: Key) {
        if (key.isUnknown) {
            return
        }
        if (key.isGlobalAction) {
            handleGlobalActionKey(key)
            return
        }
        focusedInputNode?.run {
            if (!isFocused || !isEditable) {
                return
            }
            if (key.isCharacter) {
                insertText(this, key.id.toChar().toString())
                refresh()
                return
            }
            handleNonCharKey(this, key)
        }
    }

    private fun handleGlobalActionKey(key: Key) {
        if (!key.isGlobalAction) {
            return
        }
        when(key.keyCode) {
            KEYCODE_ESCAPE -> performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun keyUp(key: Key) {

    }

    companion object {
        private const val TAG = "BarrierAccessibilitySer"
    }
}