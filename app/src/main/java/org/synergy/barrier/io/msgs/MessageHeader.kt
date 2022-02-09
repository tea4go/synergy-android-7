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

import org.synergy.barrier.io.msgs.MessageType.Companion.fromString
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Describes a message header
 *
 * Size =
 * length of message type +
 * message data size (see Message.write)
 */
class MessageHeader {
    /**
     * Get the size of the data in the message
     */
    private var size: Int

    /**
     * Set the size of the DATA passed along with this message
     */
    var dataSize: Int?

    /**
     * Get the message type for the message this header describes
     */
    var type: MessageType
        private set

    constructor(type: MessageType) {
        this.type = type
        size = type.value.length
        dataSize = null // User must set
    }

    constructor(type: String) {
        this.type = fromString(type)
        size = this.type.value.length
        dataSize = null // User must set
    }

    /**
     * Read in a message header
     * @param din Data input stream from socket
     */
    constructor(din: DataInputStream) {
        val messageSize = din.readInt()
        val messageTypeBytes = ByteArray(MESSAGE_TYPE_SIZE)
        din.read(messageTypeBytes, 0, MESSAGE_TYPE_SIZE)
        type = fromString(String(messageTypeBytes))
        size = MESSAGE_TYPE_SIZE
        dataSize = messageSize - size
    }

    @Throws(IOException::class)
    fun write(dout: DataOutputStream) {
        val dataSize = dataSize ?: throw IOException("Message header size is null")
        dout.writeInt(size + dataSize)
        dout.write(type.value.toByteArray(charset("UTF8")))
    }

    override fun toString(): String {
        return "MessageHeader:$size:$dataSize:$type"
    }

    companion object {
        private const val MESSAGE_TYPE_SIZE = 4
    }
}