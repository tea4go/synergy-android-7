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

import android.util.Log
import org.synergy.barrier.base.EventQueue
import org.synergy.barrier.base.EventQueueTimer
import org.synergy.barrier.base.EventType
import org.synergy.barrier.base.utils.Log.Companion.debug
import org.synergy.barrier.base.utils.Log.Companion.debug1
import org.synergy.barrier.base.utils.Log.Companion.error
import org.synergy.barrier.base.utils.Log.Companion.info
import org.synergy.barrier.base.utils.Log.Companion.note
import org.synergy.barrier.client.ServerProxy.Parser
import org.synergy.barrier.io.Stream
import org.synergy.barrier.io.msgs.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class ServerProxy(
    private val client: Client,
    private val stream: Stream,
    eventQueue: EventQueue,
) {
    private var seqNum = 0
    private var parser: Parser = Parser { parseHandshakeMessage() }
    private var keepAliveAlarm = 0.0
    private var keepAliveAlarmTimer: EventQueueTimer? = null
    private var din: DataInputStream? = null
    private var dout: DataOutputStream? = null

    // /**
    //  * Handle messages after the handshake is complete
    //  */
    // var messageDataBuffer = ByteArray(256)

    init {
        // handle data on stream
        eventQueue.adoptHandler(
            EventType.STREAM_INPUT_READY,
            stream.getEventTarget()
        ) { handleData() }

        // send heartbeat
        setKeepAliveRate(KEEP_ALIVE_RATE)
    }

    /**
     * Handle messages before handshake is complete
     */
    @Throws(IOException::class)
    private fun parseHandshakeMessage(): Result {
        val din = din ?: throw RuntimeException("din is null")
        // Read the header
        val header = MessageHeader(din)
        debug("Received Header: $header")
        when (header.type) {
            MessageType.QINFO ->                 //queryInfo (new QueryInfoMessage ());
                queryInfo()
            MessageType.CINFOACK -> infoAcknowledgment()
            MessageType.DSETOPTIONS -> {
                val setOptionsMessage = SetOptionsMessage(header, din)
                setOptions(setOptionsMessage)

                // handshake is complete
                debug("Handshake is complete")
                parser = Parser { parseMessage() }
                client.handshakeComplete()
            }
            MessageType.CRESETOPTIONS -> resetOptions(ResetOptionsMessage(din))
            else -> return Result.UNKNOWN
        }
        return Result.OKAY
    }

    @Throws(IOException::class)
    private fun parseMessage(): Result {
        val din = din ?: throw RuntimeException("din is null")
        // Read the header
        val header = MessageHeader(din)
        when (header.type) {
            MessageType.DMOUSEMOVE -> {
                // Cut right to the chase with mouse movements since
                //  they are the most abundant
                val ax = din.readShort()
                val ay = din.readShort()
                client.mouseMove(ax.toInt(), ay.toInt())
            }
            MessageType.DMOUSERELMOVE -> {
                val rx = din.readShort()
                val ry = din.readShort()
                client.relMouseMove(rx.toInt(), ry.toInt())
            }
            MessageType.DMOUSEWHEEL -> mouseWheel(MouseWheelMessage(din))
            MessageType.DKEYDOWN -> keyDown(KeyDownMessage(din))
            MessageType.DKEYUP -> keyUp(KeyUpMessage(din))
            MessageType.DKEYREPEAT -> keyRepeat(KeyRepeatMessage(din))
            MessageType.DMOUSEDOWN -> mouseDown(MouseDownMessage(din))
            MessageType.DMOUSEUP -> mouseUp(MouseUpMessage(din))
            MessageType.CKEEPALIVE -> {
                // echo keep alives and reset alarm
                KeepAliveMessage().write(dout!!)
                resetKeepAliveAlarm()
            }
            MessageType.CNOOP -> {}
            MessageType.CENTER -> enter(EnterMessage(header, din))
            MessageType.CLEAVE -> leave(LeaveMessage(din))
            MessageType.CCLIPBOARD -> grabClipboard(ClipboardMessage(din))
            MessageType.CSCREENSAVER -> {
                val screenSaverOnFlag = din.readByte()
                screensaver(ScreenSaverMessage(din, screenSaverOnFlag))
            }
            MessageType.QINFO -> queryInfo()
            MessageType.CINFOACK ->                 //infoAcknowledgment (new InfoAckMessage (din));
                infoAcknowledgment()
            MessageType.DCLIPBOARD -> setClipboard(ClipboardDataMessage(header, din))
            MessageType.CRESETOPTIONS -> resetOptions(ResetOptionsMessage(din))
            MessageType.DSETOPTIONS -> {
                val setOptionsMessage = SetOptionsMessage(header, din)
                setOptions(setOptionsMessage)
            }
            MessageType.CCLOSE -> {
                // server wants us to hangup
                debug1("recv close")
                // client.disconnect (null);
                return Result.DISCONNECT
            }
            MessageType.EBAD -> {
                error("server disconnected due to a protocol error")
                // client.disconnect("server reported a protocol error");
                return Result.DISCONNECT
            }
            else -> return Result.UNKNOWN
        }
        return Result.OKAY
    }

    private fun handleData() {
        val din = DataInputStream(stream.getInputStream()).also { din = it }
        // this.dout = new DataOutputStream (stream.getOutputStream ());
        // this.oout = new ObjectOutputStream (stream.getOutputStream());
        while (true) {
            when (parser.parse()) {
                Result.OKAY -> {}
                Result.UNKNOWN -> {
                    error("invalid message from server: " + din.readUTF())
                    return
                }
                Result.DISCONNECT -> return
            }
        }
    }

    private fun resetKeepAliveAlarm() {
        if (keepAliveAlarmTimer != null) {
            keepAliveAlarmTimer!!.cancel()
            keepAliveAlarmTimer = null
        }
        if (keepAliveAlarm > 0.0) {
            keepAliveAlarmTimer = EventQueueTimer(
                keepAliveAlarm, true, this
            ) { handleKeepAliveAlarm() }
        }
    }

    @Suppress("SameParameterValue")
    private fun setKeepAliveRate(rate: Double) {
        keepAliveAlarm = rate * KEEP_ALIVE_UNTIL_DEATH
        resetKeepAliveAlarm()
    }

    private fun handleKeepAliveAlarm() {
        note("server is dead")
        client.disconnect("server is not responding")
    }

    private fun queryInfo() {
        val info = ClientInfo(client.shape, client.cursorPos)
        sendInfo(info)
    }

    private fun sendInfo(info: ClientInfo) {
        try {
            val dout = DataOutputStream(stream.getOutputStream()).also { dout = it }
            InfoMessage(
                info.screenPosition.left,
                info.screenPosition.top,
                info.screenPosition.right,
                info.screenPosition.bottom,
                info.cursorPos.x,
                info.cursorPos.y
            ).write(dout)
        } catch (e: Exception) {
            Log.e(TAG, "sendInfo: ", e)
        }
    }

    private fun infoAcknowledgment() {
        debug("recv info acknowledgment")
    }

    fun onInfoChanged() {
        // send info update
        queryInfo()
    }

    private fun enter(enterMessage: EnterMessage) {
        debug1("Screen entered: $enterMessage")
        seqNum = enterMessage.sequenceNumber
        client.enter(enterMessage)
    }

    private fun leave(leaveMessage: LeaveMessage) {
        debug1("Screen left: $leaveMessage")
        client.leave(leaveMessage)
    }

    private fun keyUp(keyUpMessage: KeyUpMessage) {
        debug1(keyUpMessage.toString())
        try {
            client.keyUp(keyUpMessage.id, keyUpMessage.mask, keyUpMessage.button)
        } catch (e: Exception) {
        }
    }

    private fun keyDown(keyDownMessage: KeyDownMessage) {
        info(keyDownMessage.toString())
        client.keyDown(keyDownMessage.id, keyDownMessage.mask, keyDownMessage.button)
    }

    private fun keyRepeat(keyRepeatMessage: KeyRepeatMessage) {
        debug1(keyRepeatMessage.toString())
        client.keyRepeat(
            keyRepeatMessage.id,
            keyRepeatMessage.mask,
            keyRepeatMessage.count,
            keyRepeatMessage.button,
        )
    }

    private fun mouseDown(mouseDownMessage: MouseDownMessage) {
        debug(mouseDownMessage.toString())
        client.mouseDown(mouseDownMessage.getButtonId())
    }

    private fun mouseUp(mouseUpMessage: MouseUpMessage) {
        debug(mouseUpMessage.toString())
        client.mouseUp(mouseUpMessage.getButtonId())
    }

    private fun mouseWheel(mouseWheelMessage: MouseWheelMessage) {
        client.mouseWheel(mouseWheelMessage.getXDelta(), mouseWheelMessage.getYDelta())
    }

    private fun resetOptions(resetOptionsMessage: ResetOptionsMessage) {}

    private fun setOptions(setOptionsMessage: SetOptionsMessage) {}

    private fun screensaver(screenSaverMessage: ScreenSaverMessage) {}

    private fun grabClipboard(clipboardMessage: ClipboardMessage) {}

    private fun setClipboard(clipboardDataMessage: ClipboardDataMessage) {
        debug1("Setting clipboard: $clipboardDataMessage")
    }

    /**
     * Enumeration and Interface for parsing function
     */
    private enum class Result {
        OKAY, UNKNOWN, DISCONNECT
    }

    private enum class EResult {
        OKAY, UNKNOWN, DISCONNECT
    }

    // To define what should parse and process messages
    private fun interface Parser {
        @Throws(IOException::class)
        fun parse(): Result
    }

    companion object {
        private val TAG = ServerProxy::class.simpleName
        private const val KEEP_ALIVE_UNTIL_DEATH = 3.0
        private const val KEEP_ALIVE_RATE = 3.0
    }
}