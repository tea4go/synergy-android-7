package org.synergy.services

import android.content.Intent
import android.graphics.Point

sealed class BarrierAccessibilityAction(val intentAction: String) {
    abstract fun getIntent(): Intent

    class MouseEnter : BarrierAccessibilityAction(MOUSE_ENTER_ACTION) {
        override fun getIntent(): Intent = Intent().apply {
            action = intentAction
        }

        companion object {
            fun parseIntent() = MouseEnter()
        }
    }

    class MouseLeave : BarrierAccessibilityAction(MOUSE_LEAVE_ACTION) {
        override fun getIntent(): Intent = Intent().apply {
            action = intentAction
        }

        companion object {
            fun parseIntent() = MouseLeave()
        }
    }

    data class MouseMove(val x: Int, val y: Int) : BarrierAccessibilityAction(MOUSE_MOVE_ACTION) {
        override fun getIntent(): Intent = Intent().apply {
            action = intentAction
            putExtra("mouseX", x)
            putExtra("mouseY", y)
        }

        companion object {
            fun parseIntent(intent: Intent): MouseMove {
                val x = intent.getIntExtra("mouseX", -1)
                val y = intent.getIntExtra("mouseY", -1)
                return MouseMove(x, y)
            }
        }
    }

    data class MouseClick(val x: Int, val y: Int) : BarrierAccessibilityAction(MOUSE_CLICK_ACTION) {
        override fun getIntent(): Intent = Intent().apply {
            action = intentAction
            putExtra("mouseX", x)
            putExtra("mouseY", y)
        }

        companion object {
            fun parseIntent(intent: Intent): MouseClick {
                val x = intent.getIntExtra("mouseX", -1)
                val y = intent.getIntExtra("mouseY", -1)
                return MouseClick(x, y)
            }
        }
    }

    data class MouseLongClick(val x: Int, val y: Int) : BarrierAccessibilityAction(
        MOUSE_LONG_CLICK_ACTION,
    ) {
        override fun getIntent(): Intent = Intent().apply {
            action = intentAction
            putExtra("mouseX", x)
            putExtra("mouseY", y)
        }

        companion object {
            fun parseIntent(intent: Intent): MouseLongClick {
                val x = intent.getIntExtra("mouseX", -1)
                val y = intent.getIntExtra("mouseY", -1)
                return MouseLongClick(x, y)
            }
        }
    }

    data class Drag(
        val dragPoints: List<Point>,
        val duration: Long,
    ) : BarrierAccessibilityAction(DRAG_ACTION) {
        override fun getIntent(): Intent = Intent().apply {
            action = intentAction
            putExtra("xArray", dragPoints.map { it.x }.toIntArray())
            putExtra("yArray", dragPoints.map { it.y }.toIntArray())
            putExtra("duration", duration)
        }

        companion object {
            fun parseIntent(intent: Intent): Drag = intent.run {
                val xArray = getIntArrayExtra("xArray") ?: IntArray(0)
                val yArray = getIntArrayExtra("yArray") ?: IntArray(0)
                val duration = getLongExtra("duration", 0)
                return Drag(
                    xArray.zip(yArray).map { (x, y) -> Point(x, y) },
                    duration,
                )
            }
        }
    }

    companion object {
        private const val MOUSE_ENTER_ACTION = "mouse_enter"
        private const val MOUSE_LEAVE_ACTION = "mouse_leave"
        private const val MOUSE_MOVE_ACTION = "mouse_move"
        private const val MOUSE_CLICK_ACTION = "mouse_click"
        private const val MOUSE_LONG_CLICK_ACTION = "mouse_long_click"
        private const val DRAG_ACTION = "drag"

        private val actionMap = mapOf(
            MOUSE_ENTER_ACTION to MouseEnter,
            MOUSE_LEAVE_ACTION to MouseLeave,
            MOUSE_MOVE_ACTION to MouseMove,
            MOUSE_CLICK_ACTION to MouseClick,
            MOUSE_LONG_CLICK_ACTION to MouseLongClick,
            DRAG_ACTION to Drag,
        )

        fun getAllActions() = actionMap.keys

        fun getActionFromIntent(intent: Intent) = when (actionMap[intent.action]) {
            MouseEnter -> MouseEnter.parseIntent()
            MouseLeave -> MouseLeave.parseIntent()
            MouseMove -> MouseMove.parseIntent(intent)
            MouseClick -> MouseClick.parseIntent(intent)
            MouseLongClick -> MouseLongClick.parseIntent(intent)
            Drag -> Drag.parseIntent(intent)
            else -> null
        }
    }
}
