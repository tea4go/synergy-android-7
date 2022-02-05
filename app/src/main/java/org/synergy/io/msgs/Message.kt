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

import org.synergy.base.utils.Log.Companion.debug5
import org.synergy.io.MessageDataOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Writing:
 * Write to a ByteArrayOutputStream using a DataOutputStream
 * Create the header
 */
abstract class Message {
    /**
     * Get the message type
     */
    var type: MessageType?
        private set

    var header: MessageHeader?
        private set

    private var data: ByteArrayOutputStream

    @JvmField
    protected var dataStream: MessageDataOutputStream

    /**
     * This constructor is called when a message is read in.
     * The header information is therefore not important
     */
    protected constructor() {
        type = null
        header = null
        data = ByteArrayOutputStream()
        dataStream = MessageDataOutputStream(data)
    }

    /*
     * Create a message
     */
    constructor(type: MessageType) {
        this.type = type
        header = MessageHeader(this.type)
        data = ByteArrayOutputStream()
        dataStream = MessageDataOutputStream(data)
    }

    /**
     * Create a message with a message header
     */
    constructor(header: MessageHeader) {
        this.header = header
        type = header.type
        data = ByteArrayOutputStream()
        dataStream = MessageDataOutputStream(data)
    }

    /**
     * Get the bytes for this message
     *
     * @return message data in bytes
     */
    protected val bytes: ByteArray
        get() = data.toByteArray()

    /**
     * Writes the message data to the byte array stream
     */
    @Throws(IOException::class)
    protected open fun writeData() {
        throw IOException("Invalid message. Subclass-ed messages must implement writeData")
    }

    /**
     * Write the message header and the message data
     */
    @Throws(IOException::class)
    fun write(dout: DataOutputStream) {
        debug5("Message: Sending: $this")

        // Write the message data to the byte array.  
        //  Subclasses MUST override this function
        writeData()

        // Set the message header size based on how much data
        //  has been written to the byte array stream
        header?.dataSize = dataStream.size()
        debug5("Message: Sending Header: $header")

        // Write out the header and the message data
        header?.write(dout)
        data.writeTo(dout)
        dout.flush()
    }

    companion object {
        /**
         * Constants for reading messages
         */
        protected const val BYTE_SIZE = 1
        protected const val SHORT_SIZE = 2
        const val INT_SIZE = 4
    }
}