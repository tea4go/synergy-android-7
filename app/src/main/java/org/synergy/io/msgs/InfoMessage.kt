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
package org.synergy.io.msgs

import java.io.IOException

class InfoMessage(
    screenX: Int,
    screenY: Int,
    screenWidth: Int,
    screenHeight: Int,
    cursorX: Int,
    cursorY: Int,
) : Message(MESSAGE_TYPE) {
    var screenX: Short = screenX.toShort()
    var screenY: Short = screenY.toShort()
    var screenWidth: Short = screenWidth.toShort()
    var screenHeight: Short = screenHeight.toShort()
    var cursorX: Short = cursorX.toShort()
    var cursorY: Short = cursorY.toShort()

    // TODO: I haven't figured out what this is used for yet
    var unknown: Short = 0

    @Throws(IOException::class)
    override fun writeData() = dataStream.run {
        writeShort(screenX.toInt())
        writeShort(screenY.toInt())
        writeShort(screenWidth.toInt())
        writeShort(screenHeight.toInt())
        writeShort(unknown.toInt())
        writeShort(cursorX.toInt())
        writeShort(cursorY.toInt())
    }

    override fun toString(): String {
        return "InfoMessage:$screenX:$screenY:$screenWidth:$screenHeight:$unknown:$cursorX:$cursorY"
    }

    companion object {
        private val MESSAGE_TYPE = MessageType.DINFO
    }
}