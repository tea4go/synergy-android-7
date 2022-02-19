/*
 * synergy -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Synergy Project
 * Copyright (C) 2009 The Synergy+ Project
 * Copyright (C) 2002 Chris Schoeneman
 *
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 *
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.synergy.barrier.common.screens

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.ViewConfiguration
import android.view.ViewConfiguration.getTapTimeout
import org.synergy.utils.Timber
import org.synergy.utils.d1
import org.synergy.barrier.common.key.BarrierKeyEvent
import org.synergy.services.BarrierAccessibilityAction.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


class BasicScreen(private val context: Context) : ScreenInterface {
    private var cursorPosition = Point(-1, -1)
    private var isMouseDown = false
    private var mouseDownCursorPosition: Point? = null
    private var isDragging = false
    private val dragPoints = mutableListOf<Point>()
    private var dragStartTime: Long = 0
    private var dragEndTime: Long = 0
    private val downKeys = mutableMapOf<Int, Long>()

    // Screen dimensions
    private var width = 0
    private var height = 0

    private val worker = Executors.newSingleThreadScheduledExecutor()
    private var longClickFuture: ScheduledFuture<*>? = null

    private val dragThreshold = ViewConfiguration.get(context).scaledTouchSlop

    /**
     * Set the shape of the screen -- set from the initializing activity
     *
     * @param width
     * @param height
     */
    fun setShape(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun getShape(): Rect {
        return Rect(0, 0, width, height)
    }

    override fun enable() {}

    override fun disable() {}

    override fun enter(toggleMask: Int) {
        context.sendBroadcast(MouseEnter().getIntent())
    }

    override fun leave(): Boolean {
        context.sendBroadcast(MouseLeave().getIntent())
        return true
    }

    override fun keyDown(id: Int, mask: Int, button: Int) {
        Timber.d1("keyDown: id: $id, mask: $mask, button: $button")
        val date = Date()
        downKeys[id] = date.time
        context.sendBroadcast(
            KeyEvent(
                BarrierKeyEvent(
                    downTime = date.time,
                    eventTime = date.time,
                    action = ACTION_DOWN,
                    id = id,
                    mask = mask,
                    scanCode = button,
                )
            ).getIntent()
        )
    }

    override fun keyUp(id: Int, mask: Int, button: Int) {
        Timber.d1("keyUp: id: $id, mask: $mask, button: $button")
        val downTime = downKeys[id] ?: 0
        downKeys.remove(id)
        val date = Date()
        context.sendBroadcast(
            KeyEvent(
                BarrierKeyEvent(
                    downTime = downTime,
                    eventTime = date.time,
                    action = ACTION_UP,
                    id = id,
                    mask = mask,
                    scanCode = button,
                )
            ).getIntent()
        )
    }

    override fun keyRepeat(id: Int, mask: Int, count: Int, button: Int) {
        // Timber.d1("keyRepeat: id: $id, mask: $mask, count: $count, button: $button")
        keyDown(id, mask, button)
    }

    override fun mouseDown(buttonID: Int) {
        isMouseDown = true
        mouseDownCursorPosition = Point(cursorPosition)
        scheduleLongClick()
    }

    private fun scheduleLongClick() {
        longClickFuture?.cancel(true)
        longClickFuture = worker.schedule(
            {
                context.sendBroadcast(
                    MouseLongClick(
                        cursorPosition.x,
                        cursorPosition.y
                    ).getIntent()
                )
            },
            ViewConfiguration.getLongPressTimeout().toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    override fun mouseUp(buttonID: Int) {
        isMouseDown = false
        if (isDragging) {
            dragEndTime = Date().time
            isDragging = false
            context.sendBroadcast(Drag(dragPoints, dragEndTime - dragStartTime).getIntent())
            dragPoints.clear()
            dragStartTime = 0
            dragEndTime = 0
            // was dragging, so skip the click event
            return
        }
        if (longClickFuture?.isDone == true) {
            // Long click event sent, skip the click event
            return
        }
        longClickFuture?.cancel(true)
        context.sendBroadcast(MouseClick(cursorPosition.x, cursorPosition.y).getIntent())
    }

    override fun mouseMove(x: Int, y: Int) {
        // this state appears to signal a screen exit, use this to
        // flag mouse position reinitialization for next call
        // to this method.
        if (x == width && y == height) {
            clearMousePosition()
            return
        }

        cursorPosition.set(x, y)

        if (isMouseDown) {
            checkForDrag()
        }

        context.sendBroadcast(MouseMove(cursorPosition.x, cursorPosition.y).getIntent())
    }

    private fun checkForDrag() {
        if (isDragging) {
            // directly add cursor position to dragPoints, without checking dx, dy
            dragPoints.add(Point(cursorPosition))
            return
        }
        mouseDownCursorPosition?.run {
            val dx = cursorPosition.x - this.x
            val dy = cursorPosition.y - this.y

            if (dx.absoluteValue >= dragThreshold || dy.absoluteValue >= dragThreshold) {
                longClickFuture?.cancel(true)
                isDragging = true
                dragStartTime = Date().time
                dragPoints.clear()
                dragPoints.add(Point(cursorPosition))
            }
        }
    }

    override fun mouseRelativeMove(x: Int, y: Int) {}

    override fun mouseWheel(x: Int, y: Int) {
        val center = Point(width / 2, height / 2)
        val point = Point(center).apply { offset(x, y) }
        point.x = point.x.coerceIn(1, width - 1)
        point.y = point.y.coerceIn(1, height - 1)
        val dragPoints = listOf(center, point)
        context.sendBroadcast(Drag(dragPoints, getTapTimeout().toLong()).getIntent())
    }

    private fun clearMousePosition() {
        longClickFuture?.cancel(true)
        cursorPosition.set(-1, -1)
        isMouseDown = false
        mouseDownCursorPosition = null
        isDragging = false
        dragPoints.clear()
        dragStartTime = 0
        dragEndTime = 0
        downKeys.clear()
    }

    override fun getCursorPos(): Point {
        return cursorPosition
    }

    override fun getEventTarget(): Any {
        return this
    }
}