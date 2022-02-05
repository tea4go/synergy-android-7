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
package org.synergy.io

import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

class MessageDataInputStream(`in`: InputStream) : DataInputStream(`in`) {
    /**
     * Read in a string.  First reads in the string length and then the string
     */
    @Throws(IOException::class)
    fun readString(): String {
        val stringLength = readInt()

        // Read in the bytes and convert to a string
        val stringBytes = ByteArray(stringLength)
        read(stringBytes, 0, stringBytes.size)
        return String(stringBytes)
    }

    /**
     * Read an expected string from the stream
     * @throws IOException if expected string is not read
     */
    @Throws(IOException::class)
    fun readExpectedString(expectedString: String) {
        val stringBytes = ByteArray(expectedString.length)

        // Read in the bytes and convert to a string
        read(stringBytes, 0, stringBytes.size)
        val readString = String(stringBytes)
        if (readString != expectedString) {
            throw IOException("Expected string $expectedString not found.  Found: $readString")
        }
    }
}