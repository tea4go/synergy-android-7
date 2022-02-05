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
package org.synergy.base

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class SimpleEventQueueBuffer : EventQueueBuffer {
    private val queue: BlockingQueue<Int>

    init {
        // TODO: NOTE: This WAS a LinkedBlockingDeque but Android does not support that
        //
        // Need to reevaluate the workings there and make sure everything is going to work
        queue = LinkedBlockingQueue()
    }

    @Throws(InterruptedException::class)
    override fun getEvent(): EventData {
        val dataID = queue.take()
        return EventData(EventData.Type.USER, null, dataID)
    }

    @Throws(InterruptedException::class)
    override fun getEvent(timeout: Double): EventData {
        val dataID = queue.poll((timeout * 1000.0).toLong(), TimeUnit.MILLISECONDS)
        return EventData(EventData.Type.USER, null, dataID)
    }

    @Throws(InterruptedException::class)
    override fun addEvent(dataID: Int) {
        queue.put(dataID)
    }

    override val isEmpty: Boolean
        get() = queue.isEmpty()
}