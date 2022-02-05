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

/*
 * @author Shaun Patterson
 *
 * For lack of a better name...
 *
 * This class holds an event, a data id, and the type of
 *  event
 */
class EventData {
    enum class Type {
        NONE,
        SYSTEM,
        USER
    }

    var type: Type
        private set
    var event: Event?
        private set
    var dataID: Int
        private set

    // None
    constructor() {
        type = Type.NONE
        dataID = -1
        event = null
    }

    constructor(type: Type, event: Event?, dataID: Int) {
        this.type = type
        this.event = event
        this.dataID = dataID
    }
}