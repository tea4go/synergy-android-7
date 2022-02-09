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
package org.synergy.barrier.io.msgs

import java.io.DataInputStream
import java.io.IOException

class SetOptionsMessage(header: MessageHeader, din: DataInputStream) : Message() {

    init {
        val options = ArrayList<Int>()

        // Read off a list of integers until all the data defined in the header has been read
        var dataLeft = header.dataSize ?: 0
        while (dataLeft > 0) {
            options.add(Integer.valueOf(din.readInt()))
            dataLeft -= INT_SIZE
        }
        if (dataLeft != 0) {
            throw IOException("Error reading SetOptionsMessage. dataLeft: $dataLeft")
        }
    }

    override fun toString() = "SetOptionsMessage:"

    companion object {
        val MESSAGE_TYPE = MessageType.DSETOPTIONS
    }
}