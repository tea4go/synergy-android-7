package org.synergy.utils

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.WindowManager

object DisplayUtils {
    fun getDisplayBounds(context: Context): Rect? {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect(windowManager.currentWindowMetrics.bounds)
        } else {
            val display = windowManager.defaultDisplay ?: return null
            Rect().apply {
                display.getRectSize(this)
            }
        }
    }
}