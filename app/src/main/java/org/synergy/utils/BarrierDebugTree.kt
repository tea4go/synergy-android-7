package org.synergy.utils

import android.util.Log
import org.synergy.utils.BarrierDebugTree.Companion.DEBUG
import org.synergy.utils.BarrierDebugTree.Companion.DEBUG1
import org.synergy.utils.BarrierDebugTree.Companion.DEBUG2
import org.synergy.utils.BarrierDebugTree.Companion.DEBUG3
import org.synergy.utils.BarrierDebugTree.Companion.DEBUG4
import org.synergy.utils.BarrierDebugTree.Companion.DEBUG5
import org.synergy.utils.BarrierDebugTree.Companion.ERROR
import org.synergy.utils.BarrierDebugTree.Companion.FATAL
import org.synergy.utils.BarrierDebugTree.Companion.INFO
import org.synergy.utils.BarrierDebugTree.Companion.NOTE
import org.synergy.utils.BarrierDebugTree.Companion.PRINT
import org.synergy.utils.BarrierDebugTree.Companion.WARNING

class BarrierDebugTree : Timber.DebugTree() {

    private val className = BarrierDebugTree::class.java.name

    override val fqcnIgnore = super.fqcnIgnore + className + "${className}Kt"

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < logLevel) {
            return
        }
        super.log(getLevel(priority), tag, message, t)
    }

    private fun getLevel(priority: Int) = levelMap[priority] ?: Log.DEBUG

    companion object {
        const val FATAL = 12
        const val ERROR = 11
        const val WARNING = 10
        const val DEBUG = 9
        const val DEBUG1 = 8
        const val DEBUG2 = 7
        const val DEBUG3 = 6
        const val DEBUG4 = 5
        const val DEBUG5 = 4
        const val NOTE = 3
        const val INFO = 2
        const val PRINT = 1

        var logLevel = DEBUG

        private val levelMap = mapOf(
            FATAL to Log.ERROR,
            ERROR to Log.ERROR,
            WARNING to Log.WARN,
            DEBUG to Log.DEBUG,
            DEBUG1 to Log.DEBUG,
            DEBUG2 to Log.DEBUG,
            DEBUG3 to Log.DEBUG,
            DEBUG4 to Log.DEBUG,
            DEBUG5 to Log.DEBUG,
            NOTE to Log.INFO,
            INFO to Log.INFO,
            PRINT to Log.VERBOSE,
        )
    }
}

fun Timber.Forest.f(message: String) = log(FATAL, "[FATAL] $message")
fun Timber.Forest.f(t: Throwable) = log(FATAL, t, "[FATAL]")
fun Timber.Forest.f(message: String, t: Throwable) = log(FATAL, t, "[FATAL] $message")
fun Timber.Forest.e(message: String) = log(ERROR, "[ERROR] $message")
fun Timber.Forest.e(t: Throwable) = log(ERROR, t, "[ERROR]")
fun Timber.Forest.e(message: String, t: Throwable) = log(ERROR, t, "[ERROR] $message")
fun Timber.Forest.w(message: String) = log(WARNING, "[WARN] $message")
fun Timber.Forest.d(message: String) = log(DEBUG, "[DEBUG] $message")
fun Timber.Forest.d1(message: String) = log(DEBUG1, "[DEBUG1] $message")
fun Timber.Forest.d2(message: String) = log(DEBUG2, "[DEBUG2] $message")
fun Timber.Forest.d3(message: String) = log(DEBUG3, "[DEBUG3] $message")
fun Timber.Forest.d4(message: String) = log(DEBUG4, "[DEBUG4] $message")
fun Timber.Forest.d5(message: String) = log(DEBUG5, "[DEBUG5] $message")
fun Timber.Forest.n(message: String) = log(NOTE, "[NOTE] $message")
fun Timber.Forest.i(message: String) = log(INFO, "[INFO] $message")
fun Timber.Forest.p(message: String) = log(PRINT, "[PRINT] $message")
fun Timber.Forest.v(message: String) = log(PRINT, "[VERBOSE] $message")
