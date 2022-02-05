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
package org.synergy.net

import org.synergy.base.Event
import org.synergy.base.EventQueue
import org.synergy.base.EventType
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class TCPSocket : DataSocketInterface {
    private val socket = Socket()
    private var connected = false
    private var readable = false
    private var writable = false

    private fun onConnected() {
        connected = true
        readable = true
        writable = true
    }

    override fun bind(address: NetworkAddress) {}

    override fun close() {
        try {
            socket.close()
        } catch (e: IOException) {
        }
    }

    override fun isReady() = connected

    @Throws(IOException::class)
    override fun getInputStream(): InputStream = socket.getInputStream()

    @Throws(IOException::class)
    override fun getOutputStream(): OutputStream = socket.getOutputStream()

    @Throws(IOException::class)
    override fun connect(address: NetworkAddress) {
        socket.connect(
            InetSocketAddress(address.address, address.port),
            SOCKET_CONNECTION_TIMEOUT_IN_MILLIS
        )

        // Turn off Nagle's algorithm and set traffic type (RFC 1349) to minimize delay
        // to avoid mouse pointer "lagging"
        socket.tcpNoDelay = true
        socket.trafficClass = 8
        sendEvent(EventType.SOCKET_CONNECTED)
        onConnected()
        sendEvent(EventType.STREAM_INPUT_READY)
    }

    override fun getEventTarget() = this

    private fun sendEvent(eventType: EventType) {
        EventQueue.getInstance().addEvent(Event(eventType, getEventTarget(), null))
    }

    companion object {
        private const val SOCKET_CONNECTION_TIMEOUT_IN_MILLIS = 1000
    }
}