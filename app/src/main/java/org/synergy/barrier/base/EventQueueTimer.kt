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

import java.util.*

class EventQueueTimer(
    timeout: Double, // Do it once?
    var oneShot: Boolean, // The target for the event
    var target: Any, // What will run when the timer is fired from the Event Queue
    var job: EventJobInterface
) {
    private val timer = Timer()

    init {
        timer.schedule(TimerEventTask(), (timeout * 1000.0).toLong())
    }

    /**
     * Cancel a running timer
     */
    fun cancel() {
        timer.cancel()
    }

    /**
     * This is the actual task the timer will perform.
     * This task will take the EventJob and create a new
     * timer event and put it onto the EventQueue.  The Event Queue will
     * actually handle the dispatching of the job
     *
     *
     * Hmm. Scratch that?  Just run the damn thing?
     */
    private inner class TimerEventTask : TimerTask() {
        override fun run() {
            job.run(Event(EventType.TIMER, target))

            // Log.debug ("Timer fired");
            // EventQueue.getInstance ().adoptHandler (EventType.TIMER, target, job);
            // EventQueue.getInstance ().addEvent (new Event (EventType.TIMER, target)); //, null, Event.Flags.DELIVER_IMMEDIATELY));
            if (!oneShot) {
                timer.cancel()
            }
        }
    }
}