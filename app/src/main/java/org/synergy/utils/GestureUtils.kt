package org.synergy.utils

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Point
import android.os.Build
import android.view.ViewConfiguration
import androidx.annotation.RequiresApi

object GestureUtils {
    @RequiresApi(Build.VERSION_CODES.N)
    fun click(point: Point) = GestureDescription.StrokeDescription(
        path(point),
        0,
        1L,
    )

    @RequiresApi(Build.VERSION_CODES.N)
    fun longClick(point: Point) = GestureDescription.StrokeDescription(
        path(point),
        0,
        ViewConfiguration.getLongPressTimeout().toLong()
    )

    fun path(points: List<Point>): Path {
        val first = points.first()
        val rest = if (points.size > 1) {
            points.subList(1, points.size - 1).toTypedArray()
        } else {
            emptyArray()
        }
        return path(first, *rest)
    }

    private fun path(first: Point, vararg rest: Point): Path {
        val path = Path()
        path.moveTo(first.x.toFloat(), first.y.toFloat())
        for (point in rest) {
            path.lineTo(point.x.toFloat(), point.y.toFloat())
        }
        return path
    }
}