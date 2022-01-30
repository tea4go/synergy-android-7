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
package org.synergy.common.screens

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.ViewConfiguration
import org.synergy.base.utils.Log
import org.synergy.services.BarrierAccessibilityAction.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


class BasicScreen(private val context: Context) : ScreenInterface {
    private val buttonToKeyDownID: IntArray = IntArray(256)

    // Keep track of the mouse cursor since I cannot find a way of
    //  determining the current mouse position
    private var cursorPosition = Point(-1, -1)
    private var isMouseDown = false
    private var mouseDownCursorPosition: Point? = null
    private var isDragging = false
    private val dragPoints = mutableListOf<Point>()
    private var dragStartTime: Long = 0
    private var dragEndTime: Long = 0

    // Screen dimensions
    private var width = 0
    private var height = 0

    private val worker = Executors.newSingleThreadScheduledExecutor()
    private var longClickFuture: ScheduledFuture<*>? = null

    private val dragThreshold = ViewConfiguration.get(context).scaledTouchSlop

    init {
        // the keyUp/Down/Repeat button parameter appears to be the low-level
        // keyboard scan code (*shouldn't be* more than 256 of these, but I speak
        // from anecdotal experience, not as an expert...
        Arrays.fill(buttonToKeyDownID, -1)
    }

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
        allKeysUp()
        context.sendBroadcast(MouseEnter().getIntent())
    }

    override fun leave(): Boolean {
        allKeysUp()
        context.sendBroadcast(MouseLeave().getIntent())
        return true
    }

    private fun allKeysUp() {
        // TODO Auto-generated method stub
    }

    override fun keyDown(id: Int, mask: Int, button: Int) {
        // 1) 'button - 1' appears to be the low-level keyboard scan code
        // 2) 'id' does not appear to be conserved between server keyDown
        // and keyUp event broadcasts as the 'id' on *most* keyUp events
        // appears to be set to 0.  'button' does appear to be conserved
        // so we store the keyDown 'id' using this event so that we can
        // pull out the 'id' used for keyDown for proper keyUp handling
        if (button < buttonToKeyDownID.size) {
            buttonToKeyDownID[button] = id
        } else {
            Log.note("found keyDown button parameter > " + buttonToKeyDownID.size + ", may not be able to properly handle keyUp event.")
        }
        // TODO simulate keydown press
    }

    override fun keyUp(id: Int, mask: Int, button: Int) {
        var id1 = id
        if (button < buttonToKeyDownID.size) {
            val keyDownID = buttonToKeyDownID[button]
            if (keyDownID > -1) {
                id1 = keyDownID
            }
        } else {
            Log.note("found keyUp button parameter > " + buttonToKeyDownID.size + ", may not be able to properly handle keyUp event.")
        }
        // TODO simulate keyup event
    }

    override fun keyRepeat(keyEventID: Int, mask: Int, button: Int) {}

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
        // Log.debug("mouseMove: $x, $y")

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

    override fun mouseWheel(x: Int, y: Int) {}

    private fun clearMousePosition() {
        longClickFuture?.cancel(true)
        cursorPosition.set(-1, -1)
        isMouseDown = false
        mouseDownCursorPosition = null
        isDragging = false
        dragPoints.clear()
        dragStartTime = 0
        dragEndTime = 0
    }

    override fun getCursorPos(): Point {
        return cursorPosition
    }

    override fun getEventTarget(): Any {
        return this
    }
}