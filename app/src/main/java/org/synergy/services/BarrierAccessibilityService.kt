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
import android.view.KeyEvent.*
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.*
import org.synergy.R
import org.synergy.utils.Timber
import org.synergy.utils.e
import org.synergy.barrier.common.key.*
import org.synergy.services.BarrierAccessibilityAction.*
import org.synergy.utils.AccessibilityNodeInfoUtils.MoveDirection.NEXT
import org.synergy.utils.AccessibilityNodeInfoUtils.MoveDirection.PREVIOUS
import org.synergy.utils.AccessibilityNodeInfoUtils.copyText
import org.synergy.utils.AccessibilityNodeInfoUtils.cutText
import org.synergy.utils.AccessibilityNodeInfoUtils.deleteText
import org.synergy.utils.AccessibilityNodeInfoUtils.insertText
import org.synergy.utils.AccessibilityNodeInfoUtils.moveCursor
import org.synergy.utils.AccessibilityNodeInfoUtils.pasteText
import org.synergy.utils.GestureUtils.click
import org.synergy.utils.GestureUtils.longClick
import org.synergy.utils.GestureUtils.path


class BarrierAccessibilityService : AccessibilityService() {
    private lateinit var cursorView: View
    private lateinit var cursorLayout: LayoutParams
    private lateinit var windowManager: WindowManager

    private var skipModifiersOnKeyUp: Boolean = false

    private val root: AccessibilityNodeInfoCompat
        get() = wrap(rootInActiveWindow)

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
        if (!cursorView.isAttachedToWindow) {
            try {
                windowManager.addView(cursorView, cursorLayout)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
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
        if (cursorView.isAttachedToWindow) {
            try {
                windowManager.removeView(cursorView)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
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
            is KeyEvent -> keyEvent(action.keyEvent)
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

    private fun keyEvent(keyEvent: BarrierKeyEvent?) {
        // Timber.d("keyEvent: $keyEvent")
        if (keyEvent == null || !keyEvent.hasValidKeyCode) {
            return
        }
        when (keyEvent.action) {
            ACTION_DOWN -> keyDown(keyEvent)
            ACTION_UP -> keyUp(keyEvent)
            else -> return
        }
    }

    private fun keyDown(keyEvent: BarrierKeyEvent) {
        handleKeyDownEvent(keyEvent)
        // if key event had modifiers, skip handling the modifier on key up
        if (!BarrierKeyEvent.isModifierKey(keyEvent.keyCode) && keyEvent.hasModifiers) {
            skipModifiersOnKeyUp = true
        }
    }

    private fun handleKeyDownEvent(keyEvent: BarrierKeyEvent): Boolean {
        var handled = handleCharKeyEvent(keyEvent)
        if (handled) {
            return true
        }
        handled = handleOneKeyActionEvent(keyEvent)
        if (handled) {
            return true
        }
        handled = handleTwoKeyActionEvent(keyEvent)
        if (handled) {
            return true
        }
        return false
    }

    private fun keyUp(keyEvent: BarrierKeyEvent) {
        // handle modifier keys
        if (!BarrierKeyEvent.isModifierKey(keyEvent.keyCode)) {
            return
        }
        if (skipModifiersOnKeyUp) {
            // since we support at most 2-key combos, we will have only 1 modifier key pressed
            skipModifiersOnKeyUp = false
            return
        }
        val action = MODIFIER_KEY_GLOBAL_ACTION_MAP[keyEvent.keyCode] ?: return
        performGlobalAction(action)
    }

    private fun handleCharKeyEvent(keyEvent: BarrierKeyEvent): Boolean {
        // if a modifier is pressed which is not the shift key, don't handle the event here
        if (keyEvent.hasModifiers && !keyEvent.isShiftPressed || !keyEvent.isCharacter) {
            return false
        }
        val node = focusedInputNode ?: return false
        if (!node.isFocused || !node.isEditable) {
            return false
        }
        val inserted = insertText(node, keyEvent.unicodeChar.toChar().toString())
        node.refresh()
        return inserted
    }

    private fun handleOneKeyActionEvent(keyEvent: BarrierKeyEvent): Boolean {
        if (keyEvent.hasModifiers) {
            return false
        }
        if (ONE_KEY_GLOBAL_ACTION_MAP.containsKey(keyEvent.keyCode)) {
            return handleOneKeyGlobalActionEvent(keyEvent)
        }
        if (ONE_KEY_TEXT_NODE_ACTION_MAP.containsKey(keyEvent.keyCode)) {
            return handleOneKeyTextNodeActionEvent(keyEvent)
        }
        return false
    }

    /**
     * Handles key combo
     */
    private fun handleTwoKeyActionEvent(keyEvent: BarrierKeyEvent): Boolean {
        if (keyEvent.hasNoModifiers) {
            return false
        }
        // find the first matching combo
        var handled = handleTwoKeyGlobalActionEvent(keyEvent)
        if (handled) {
            return true
        }
        handled = handleTwoKeyTextNodeActionEvent(keyEvent)
        if (handled) {
            return true
        }
        return false
    }

    private fun handleOneKeyGlobalActionEvent(keyEvent: BarrierKeyEvent): Boolean {
        val action = ONE_KEY_GLOBAL_ACTION_MAP[keyEvent.keyCode] ?: return false
        return performGlobalAction(action)
    }

    private fun handleOneKeyTextNodeActionEvent(keyEvent: BarrierKeyEvent): Boolean {
        if (!ONE_KEY_TEXT_NODE_ACTION_MAP.containsKey(keyEvent.keyCode)) {
            return false
        }
        val node = focusedInputNode ?: return false
        node.refresh()
        val performed = when (keyEvent.keyCode) {
            KEYCODE_DEL -> deleteText(node)
            KEYCODE_FORWARD_DEL -> deleteText(node, true)
            KEYCODE_MOVE_HOME -> moveCursor(node, MOVEMENT_GRANULARITY_LINE, PREVIOUS)
            KEYCODE_MOVE_END -> moveCursor(node, MOVEMENT_GRANULARITY_LINE, NEXT)
            KEYCODE_DPAD_LEFT -> moveCursor(node, MOVEMENT_GRANULARITY_CHARACTER, PREVIOUS)
            KEYCODE_DPAD_RIGHT -> moveCursor(node, MOVEMENT_GRANULARITY_CHARACTER, NEXT)
            else -> false
        }
        return performed
    }

    private fun handleTwoKeyGlobalActionEvent(keyEvent: BarrierKeyEvent): Boolean {
        val (_, action) = TWO_KEY_COMBO_GLOBAL_ACTION_MAP[keyEvent.keyCode]
            ?.asSequence()
            ?.find { (key, _) -> keyEvent.hasTheseModifiers(key) }
            ?: return false
        return performGlobalAction(action)
    }

    private fun handleTwoKeyTextNodeActionEvent(keyEvent: BarrierKeyEvent): Boolean {
        val node = focusedInputNode ?: return false
        val (_, action) = TWO_KEY_COMBO_TEXT_NODE_ACTION_MAP[keyEvent.keyCode]
            ?.asSequence()
            ?.find { (key, _) -> keyEvent.hasTheseModifiers(key) }
            ?: return false

        // Timber.d("node: $node, action: $action")
        node.refresh()
        val performed = when (action) {
            ACTION_COPY -> copyText(node)
            ACTION_CUT -> cutText(node)
            ACTION_PASTE -> pasteText(node)
            ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY -> return when (keyEvent.keyCode) {
                KEYCODE_DPAD_LEFT -> moveCursor(
                    node,
                    MOVEMENT_GRANULARITY_CHARACTER,
                    PREVIOUS,
                    true
                )
                KEYCODE_MOVE_HOME -> moveCursor(
                    node,
                    MOVEMENT_GRANULARITY_LINE,
                    PREVIOUS,
                    true
                )
                else -> false
            }
            ACTION_NEXT_AT_MOVEMENT_GRANULARITY -> return when (keyEvent.keyCode) {
                KEYCODE_DPAD_RIGHT -> moveCursor(
                    node,
                    MOVEMENT_GRANULARITY_CHARACTER,
                    NEXT,
                    true
                )
                KEYCODE_MOVE_END -> moveCursor(
                    node,
                    MOVEMENT_GRANULARITY_LINE,
                    NEXT,
                    true
                )
                else -> false
            }
            else -> false
        }
        return performed
    }
}