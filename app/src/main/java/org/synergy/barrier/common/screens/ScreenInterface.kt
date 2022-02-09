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

import android.graphics.Point
import android.graphics.Rect

interface ScreenInterface {
    fun getEventTarget(): Any

    fun getShape(): Rect

    fun getCursorPos(): Point

    fun enable()

    fun disable()

    fun enter(toggleMask: Int)

    fun leave(): Boolean

    fun keyDown(id: Int, mask: Int, button: Int)

    fun keyUp(id: Int, mask: Int, button: Int)

    fun keyRepeat(id: Int, mask: Int, count: Int, button: Int)

    fun mouseDown(buttonID: Int)

    fun mouseUp(buttonID: Int)

    fun mouseMove(x: Int, y: Int)

    fun mouseRelativeMove(x: Int, y: Int)

    fun mouseWheel(x: Int, y: Int)
}