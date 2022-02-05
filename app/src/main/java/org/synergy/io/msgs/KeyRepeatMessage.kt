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

import java.io.DataInputStream

class KeyRepeatMessage(din: DataInputStream) : Message() {
    private val id: Short = din.readShort()
    private val mask: Short = din.readShort()
    private val count: Short = din.readShort()
    private val button: Short = din.readShort()

    fun getId() = id.toInt()

    fun getMask() = mask.toInt()

    fun getCount() = count.toInt()

    fun getButton() = button.toInt()

    override fun toString(): String {
        return "$MESSAGE_TYPE:$id:$mask:$button"
    }

    companion object {
        val MESSAGE_TYPE = MessageType.DKEYDOWN
    }
}