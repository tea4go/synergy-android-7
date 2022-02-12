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
package org.synergy.barrier.base

import android.util.Log
import org.synergy.barrier.base.Event.Companion.deleteData
import org.synergy.barrier.base.utils.Log.Companion.debug
import org.synergy.barrier.base.utils.Log.Companion.debug5
import org.synergy.barrier.base.utils.Log.Companion.note
import java.util.*

class EventQueue : EventQueueInterface {
    // buffer of events
    private var buffer: EventQueueBuffer? = SimpleEventQueueBuffer()

    // saved events
    private val events: MutableMap<Int, Event> = mutableMapOf()
    private val oldEventIDs: LinkedList<Int> = LinkedList()

    // event handlers
    private val handlers: MutableMap<Any, MutableMap<EventType, EventJobInterface>> = mutableMapOf()

    override val isEmpty: Boolean
        get() = buffer?.isEmpty == true && nextTimerTimeout != 0.0
    private val nextTimerTimeout: Double
        get() = 0.0

    @Synchronized
    override fun adoptBuffer(eventQueueBuffer: EventQueueBuffer?) {
        // discard old buffer and old events
        events.clear()
        oldEventIDs.clear()

        // use new buffer
        buffer = eventQueueBuffer
        if (buffer == null) {
            buffer = SimpleEventQueueBuffer()
        }
    }

    /**
     * Get an event from the event queue
     *
     *
     * TODO: The SimpleEventQueueBuffer has not been tested... and is rarely used
     * in the client
     *
     * @event Event to get
     * @timeout milliseconds to wait, < 0.0 is infinite
     */
    @Throws(InvalidMessageException::class)
    override fun getEvent(timeout: Double): Event? {
        val buffer = buffer ?: return null
        val eventData = try {
            if (timeout < 0.0) {
                // Infinite timeout, retry forever
                buffer.getEvent()
            } else {
                buffer.getEvent(timeout)
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "getEvent: ", e)
            return null
        }
        return when (eventData?.type) {
            EventData.Type.NONE -> if (timeout < 0.0) {
                // Client is not expecting a NONE type.
                // Just try again
                getEvent(timeout)
            } else null
            EventData.Type.SYSTEM -> eventData.event
            EventData.Type.USER -> removeEvent(eventData.dataID)
            else -> throw InvalidMessageException("Invalid message type: " + eventData?.type)
        }
    }

    /**
     * Dispatch an event
     */
    override fun dispatchEvent(event: Event): Boolean {
        note("dispatching: $event")
        val target = event.target ?: return false
        val job = getHandler(event.type, target)
        if (job == null) {
            debug("job is null for Event: $event")
            return false
        }
        debug("running job")
        job.run(event)
        return true
    }

    /**
     * Add an event to the queue
     */
    override fun addEvent(event: Event) {
        debug5("addEvent")
        when (event.type) {
            EventType.UNKNOWN, EventType.SYSTEM, EventType.TIMER -> {
                debug("Bogus event discarded")
                return
            }
            else -> {}
        }
        if (event.flags == Event.Flags.DELIVER_IMMEDIATELY) {
            dispatchEvent(event)

            // TODO: Questionable
            deleteData(event)
        } else {
            // store the event's data locally
            val eventID = saveEvent(event)

            // add it
            try {
                buffer?.addEvent(eventID)
            } catch (e: InterruptedException) {
                // failed to send event
                // TODO Log error?
                removeEvent(eventID)
                // TODO Questionable
                deleteData(event)
            }
        }
    }

    /**
     * Link an event to an EventJobInterface
     */
    @Synchronized
    fun adoptHandler(type: EventType, target: Any, handler: EventJobInterface) {
        // set/replace current handler
        var handlerMap = handlers[target]
        if (handlerMap == null) {
            // First handler for this event type
            handlerMap = EnumMap(EventType::class.java)
            handlers[target] = handlerMap
        }
        handlers[target]?.set(type, handler)
    }

    /**
     * Remove a handler for a particular event
     */
    @Synchronized
    fun removeHandler(type: EventType, target: Any) {
        handlers[target]?.remove(type)
    }

    /**
     * Remove all handlers for a target
     */
    fun removeHandlers(target: Any) {
        handlers.remove(target)
    }

    fun getHandler(type: EventType, target: Any) = handlers[target]?.get(type)

    @Synchronized
    private fun saveEvent(event: Event): Int {
        debug("Old EventIDs Size: " + oldEventIDs.size)

        // choose id
        val id = if (!oldEventIDs.isEmpty()) {
            // reuse an old id
            oldEventIDs.removeAt(oldEventIDs.size - 1)
        } else {
            events.size
        }

        // save data
        debug("Saving event data: $id")
        events[id] = event
        return id
    }

    @Synchronized
    private fun removeEvent(eventID: Int): Event {
        val removedEvent = events.remove(eventID)
        return if (removedEvent == null) {
            Event()
        } else {
            // push the old id for reuse
            oldEventIDs.addLast(eventID)
            removedEvent
        }
    }

    companion object {
        private val TAG = EventQueue::class.simpleName

        // private fun interrupt() {
        //     // TODO: Todo?
        // }
    }
}