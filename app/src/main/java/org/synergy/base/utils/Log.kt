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
package org.synergy.base.utils

/**
 * Logging class
 *
 * Example usage:
 * <pre>
 * Log.fatal("fatal error");
 * Log.debug1("debug1 printout")
 * Log.setLogLevel(Log.Level.FATAL)
 * </pre>
 *
 * @author Shaun Patterson
 */
@Deprecated("Use Timber instead")
class Log private constructor() {

    enum class Level {
        FATAL,
        ERROR,
        WARNING,
        DEBUG,
        DEBUG1,
        DEBUG2,
        DEBUG3,
        DEBUG4,
        DEBUG5,
        NOTE,
        INFO,
        PRINT,
    }

    private val outputters = mutableListOf<LogOutputterInterface>()
    private val alwaysOutputters = mutableListOf<LogOutputterInterface>()

    init {
        insert(AndroidLogOutputter(), false)
    }

    private fun insert(
        outputter: LogOutputterInterface,
        alwaysAtHead: Boolean
    ) {
        if (alwaysAtHead) {
            alwaysOutputters.add(outputter)
        } else {
            outputters.add(outputter)
        }
    }

    private fun remove(outputter: LogOutputterInterface) {
        outputters.remove(outputter)
    }

    fun popFront(alwaysAtHead: Boolean) {
        val list = if (alwaysAtHead) alwaysOutputters else outputters
        if (list.isNotEmpty()) {
            list.removeAt(0)
        }
    }

    private fun print(level: Level, message: String) {
        if (level > logLevel) {
            // Done
            return
        }

        // Get the calling method's class and the line number
        val stackTraceElements = Thread.currentThread().stackTrace
        val tag = "Synergy"
        var formattedMessage = message
        try {
            val caller = stackTraceElements[3].className
            val lineNum = stackTraceElements[3].lineNumber
            formattedMessage = "$caller:$lineNum : $formattedMessage"
        } catch (ignored: Exception) {
        }
        output(level, tag, level.name + ":" + formattedMessage)
    }

    private fun output(level: Level, tag: String, message: String) {
        for (outputter in alwaysOutputters) {
            outputter.write(level, tag, message)
        }
        for (outputter in outputters) {
            outputter.write(level, tag, message)
        }
    }

    companion object {
        // Current level of logging
        var logLevel = Level.DEBUG

        @Volatile
        private var instance: Log? = null

        fun getInstance() = instance ?: synchronized(this) {
            Log().also { instance = it }
        }

        fun print(message: String) {
            getInstance().print(Level.PRINT, message)
        }

        fun fatal(message: String) {
            getInstance().print(Level.FATAL, message)
        }

        fun error(message: String) {
            getInstance().print(Level.ERROR, message)
        }

        fun info(message: String) {
            getInstance().print(Level.INFO, message)
        }

        fun note(message: String) {
            getInstance().print(Level.NOTE, message)
        }

        fun debug(message: String) {
            getInstance().print(Level.DEBUG, message)
        }

        fun debug1(message: String) {
            getInstance().print(Level.DEBUG1, message)
        }

        fun debug2(message: String) {
            getInstance().print(Level.DEBUG2, message)
        }

        fun debug3(message: String) {
            getInstance().print(Level.DEBUG3, message)
        }

        fun debug4(message: String) {
            getInstance().print(Level.DEBUG4, message)
        }

        fun debug5(message: String) {
            getInstance().print(Level.DEBUG5, message)
        }
    }
}