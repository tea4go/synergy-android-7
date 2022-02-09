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
package org.synergy.barrier.base.utils

class AndroidLogOutputter : LogOutputterInterface {
    override fun open(title: String) {
        // Do nothing
    }

    override fun close() {
        // Do nothing
    }

    override fun show(showIfEmpty: Boolean) {
        // Do nothing
    }

    override fun write(level: Log.Level, tag: String, message: String): Boolean {
        when (level) {
            Log.Level.PRINT -> android.util.Log.v(tag, message)
            Log.Level.FATAL,
            Log.Level.ERROR -> android.util.Log.e(tag, message)
            Log.Level.WARNING -> android.util.Log.w(tag, message)
            Log.Level.NOTE,
            Log.Level.INFO -> android.util.Log.i(tag, message)
            Log.Level.DEBUG,
            Log.Level.DEBUG1,
            Log.Level.DEBUG2,
            Log.Level.DEBUG3,
            Log.Level.DEBUG4,
            Log.Level.DEBUG5 -> android.util.Log.d(tag, message)
        }
        return true // wtf
    }

    override fun flush() {
        System.out.flush()
    }
}