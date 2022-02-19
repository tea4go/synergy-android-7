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
package org.synergy.barrier.client

import android.graphics.Point
import android.graphics.Rect
import org.synergy.barrier.base.Event
import org.synergy.barrier.base.EventQueue
import org.synergy.barrier.base.EventTarget
import org.synergy.barrier.base.EventType
import org.synergy.barrier.base.utils.Timber
import org.synergy.barrier.base.utils.d
import org.synergy.barrier.base.utils.d1
import org.synergy.barrier.base.utils.e
import org.synergy.barrier.common.screens.ScreenInterface
import org.synergy.barrier.io.Stream
import org.synergy.barrier.io.msgs.EnterMessage
import org.synergy.barrier.io.msgs.HelloBackMessage
import org.synergy.barrier.io.msgs.HelloMessage
import org.synergy.barrier.io.msgs.LeaveMessage
import org.synergy.barrier.net.NetworkAddress
import org.synergy.barrier.net.SocketFactoryInterface
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class Client(
    private val name: String,
    private val serverAddress: NetworkAddress,
    private val socketFactory: SocketFactoryInterface,
    // private val streamFilterFactory: StreamFilterFactoryInterface?,
    private val screen: ScreenInterface,
    private val eventQueue: EventQueue,
    private val connectionChangeListener: (Boolean) -> Unit,
) : EventTarget {
    private var stream: Stream? = null
    private var mouseX = 0
    private var mouseY = 0
    private var server: ServerProxy? = null

    val shape: Rect
        get() = screen.getShape()

    val cursorPos: Point
        get() = screen.getCursorPos()

    @Throws(Throwable::class)
    fun finalize() {
        // TODO
    }

    @Throws(IOException::class)
    fun connect() {
        if (stream != null) {
            return
        }
        serverAddress.resolve()
        Timber.d("Connecting to: '${serverAddress.hostname}': ${serverAddress.address}:${serverAddress.port}")

        // create the socket
        val socket = socketFactory.create()

        // filter socket messages, including a packetizing filter
        stream = socket
        // if (streamFilterFactory != null) {
        // TODO stream = streamFilterFactory.create (stream, true);
        // }

        // connect
        Timber.d("connecting to server")
        setupConnecting()
        setupTimer()
        socket.connect(serverAddress)
        connectionChangeListener(true)
    }

    fun disconnect(msg: String? = null) {
        cleanupTimer()
        cleanupScreen()
        cleanupConnecting()
        if (msg != null) {
            sendConnectionFailedEvent(msg)
        } else {
            sendEvent(EventType.CLIENT_DISCONNECTED, null)
            stream?.close()
        }
    }

    private fun setupConnecting() = stream?.run {
        eventQueue.adoptHandler(
            EventType.SOCKET_CONNECTED,
            getEventTarget(),
        ) { handleConnected() }
        // val job = EventQueue.getInstance().getHandler(EventType.SOCKET_CONNECTED, it.eventTarget)
        eventQueue.adoptHandler(
            EventType.SOCKET_CONNECT_FAILED,
            getEventTarget(),
        ) { handleConnectionFailed() }
    }

    private fun cleanupConnecting() = stream?.run {
        eventQueue.removeHandler(
            EventType.SOCKET_CONNECTED,
            getEventTarget(),
        )
        eventQueue.removeHandler(
            EventType.SOCKET_CONNECT_FAILED,
            getEventTarget(),
        )
    }

    private fun setupTimer() {
        // TODO
    }

    private fun handleConnected() {
        Timber.d1("connected; wait for hello")
        cleanupConnecting()
        setupConnection()

        // TODO Clipboard
    }

    private fun handleConnectionFailed() {
        Timber.d("connection failed")
    }

    private fun handleDisconnected() {
        Timber.d("disconnected")
        connectionChangeListener(false)
    }

    private fun handleHello() = stream?.run {
        Timber.d1("handling hello")
        try {
            // Read in the Hello Message
            val din = DataInputStream(getInputStream())
            val helloMessage = HelloMessage(din)
            Timber.d1("Read hello message: $helloMessage")

            // TODO check versions

            // say hello back
            val out = DataOutputStream(getOutputStream())

            // Grab the hostname
            HelloBackMessage(1, 3, name).write(out)
            setupScreen()
            cleanupTimer()

            // make sure we process any remaining messages later. we won't
            //  receive another event for already pending messages so we fake
            //  one
            if (isReady()) {
                // TODO, So far this event does nothing -- I think
                eventQueue.addEvent(
                    Event(
                        EventType.STREAM_INPUT_READY,
                        getEventTarget()
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun setupConnection() = stream?.run {
        eventQueue.adoptHandler(
            EventType.SOCKET_DISCONNECTED,
            getEventTarget()
        ) { handleDisconnected() }
        eventQueue.adoptHandler(
            EventType.STREAM_INPUT_READY,
            getEventTarget()
        ) { handleHello() }
        eventQueue.adoptHandler(
            EventType.STREAM_OUTPUT_ERROR,
            getEventTarget()
        ) { handleDisconnected() }
        eventQueue.adoptHandler(
            EventType.STREAM_INPUT_SHUTDOWN,
            getEventTarget()
        ) { handleDisconnected() }
        eventQueue.adoptHandler(
            EventType.STREAM_OUTPUT_SHUTDOWN,
            getEventTarget()
        ) { handleDisconnected() }
    }

    private fun setupScreen() = stream?.run {
        server = ServerProxy(this@Client, this, eventQueue)
        eventQueue.adoptHandler(
            EventType.SHAPE_CHANGED,
            this@Client.eventTarget
        ) { handleShapeChanged() }

        // TODO Clipboard
        // EventQueue.getInstance().adoptHandler(Stream.getInputShutdownEvent(), stream.getEventTarget(),
        // new EventJobInterface () { public void run (Event event) {
        // handleDisconnected ();
        // }});
    }

    private fun cleanupTimer() {
        // TODO
    }

    private fun cleanupScreen() {
        // TODO
    }

    override val eventTarget: Any
        get() = screen.getEventTarget()

    private fun handleShapeChanged() {
        Timber.d1("resolution changed")
        server?.onInfoChanged()
    }

    fun handshakeComplete() {
        screen.enable()
        sendEvent(EventType.CLIENT_CONNECTED, "")
    }

    private fun sendEvent(type: EventType, data: Any?) {
        eventQueue.addEvent(Event(type, data))
    }

    private fun sendConnectionFailedEvent(msg: String) {
        connectionChangeListener(false)
    }

    fun enter(enterMessage: EnterMessage) {
        mouseX = enterMessage.x.toInt()
        mouseY = enterMessage.y.toInt()
        screen.mouseMove(mouseX, mouseY)
        screen.enter(enterMessage.mask.toInt())
    }

    fun leave(leaveMessage: LeaveMessage?) {
        // Since I don't know how to hide the cursor, tuck it away out of sight
        // screen.mouseMove(screen.getShape().right, screen.getShape().bottom);
        screen.leave()
    }

    fun mouseMove(x: Int, y: Int) {
        screen.mouseMove(x, y)
    }

    fun mouseDown(buttonID: Int) {
        screen.mouseDown(buttonID)
    }

    fun mouseUp(buttonID: Int) {
        screen.mouseUp(buttonID)
    }

    fun relMouseMove(x: Int, y: Int) {
        screen.mouseRelativeMove(x, y)
    }

    fun mouseWheel(x: Int, y: Int) {
        screen.mouseWheel(x, y)
    }

    /**
     * @param keyEventID A VK_ defined in KeyEvent
     */
    fun keyDown(keyEventID: Int, mask: Int, button: Int) {
        screen.keyDown(keyEventID, mask, button)
    }

    /**
     * @param keyEventID A VK_ defined in KeyEvent
     */
    fun keyRepeat(keyEventID: Int, mask: Int, count: Int, button: Int) {
        screen.keyRepeat(keyEventID, mask, count, button)
    }

    /**
     * @param keyEventID A VK_ defined in KeyEvent
     */
    fun keyUp(keyEventID: Int, mask: Int, button: Int) {
        screen.keyUp(keyEventID, mask, button)
    }
}