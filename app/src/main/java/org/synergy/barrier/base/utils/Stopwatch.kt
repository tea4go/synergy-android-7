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
/*
 * 
 *
 * Copyright (c) 2005, Corey Goldberg

 * StopWatch.java is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
*/
package org.synergy.barrier.base.utils

class Stopwatch(private var triggered: Boolean) {
    private var mark = 0.0

    var isStopped: Boolean
        private set

    private val clock: Double
        get() = System.currentTimeMillis().toDouble() / 1000.0

    init {
        isStopped = triggered
        if (!triggered) {
            mark = clock
        }
    }

    fun reset() = if (isStopped) {
        val dt = mark
        mark = 0.0
        dt
    } else {
        val t = clock
        val dt = t - mark
        mark = dt
        dt
    }

    fun stop() {
        if (isStopped) {
            return
        }

        // save the elapsed time
        mark = clock - mark
        isStopped = true
    }

    fun start() {
        triggered = false
        if (!isStopped) {
            return
        }

        // set the mark such that it reports the time elapsed at stop ()
        mark = clock - mark
        isStopped = false
    }

    fun setTrigger() {
        stop()
        triggered = true
    }

    val time: Double
        get() = when {
            triggered -> {
                val dt = mark
                start()
                dt
            }
            isStopped -> mark
            else -> clock - mark
        }

    val timeNoStart: Double
        get() = (if (isStopped) mark else clock - mark)
}