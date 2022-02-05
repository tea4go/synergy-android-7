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

import kotlin.jvm.JvmOverloads

/**
 * @author Shaun Patterson
 */
class Event @JvmOverloads constructor(
    val type: EventType = EventType.UNKNOWN,
    val target: Any? = null,
    val data: Any? = null,
    val flags: Flags = Flags.NONE,
) {
    enum class Flags(val value: Int) {
        NONE(0x00),
        DELIVER_IMMEDIATELY(0x01),
        DONTFREEDATA(0x02),
    }

    val name = ""

    override fun toString(): String {
        return "Event:$type:$target"
    }

    companion object {
        @JvmStatic
        fun deleteData(event: Event?) {
            // TODO
        }
    }
}