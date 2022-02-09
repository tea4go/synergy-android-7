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

import android.util.Log
import org.synergy.barrier.io.MessageDataInputStream
import java.io.DataInputStream
import java.io.IOException

/**
 * This message does not have a header
 */
class HelloMessage : Message {
    var majorVersion = 0
        private set
    var minorVersion = 0
        private set

    constructor(majorVersion: Int, minorVersion: Int) {
        // This message does not have a standard header
        this.majorVersion = majorVersion
        this.minorVersion = minorVersion
    }

    constructor(din: DataInputStream) {
        try {
            val mdin = MessageDataInputStream(din)
            val packetSize = mdin.readInt()
            if (packetSize != HELLO_MESSAGE_SIZE) {
                throw InvalidMessageException("Hello message not the right size: $packetSize")
            }

            // Read in "Synergy" string
            mdin.readExpectedString("Barrier")

            // Read in the major and minor protocol versions
            majorVersion = mdin.readShort().toInt()
            minorVersion = mdin.readShort().toInt()
        } catch (e: IOException) {
            Log.d("Barrier", "wrong hello message: " + e.message)
            throw InvalidMessageException(e.message)
        }
    }

    override fun toString() = "HelloMessage:$majorVersion:$minorVersion"

    companion object {
        private const val HELLO_MESSAGE_SIZE = 11
    }
}