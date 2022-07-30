package com.lesamy.warframewidgets.cetuscycle

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
import com.lesamy.warframewidgets.GetUpdate
import com.lesamy.warframewidgets.R
import com.lesamy.warframewidgets.State
import com.lesamy.warframewidgets.common.Platform
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit

class CetusCycleProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val result = runBlocking {
            GetUpdate.retrieveData(Platform.PC)
        }
        val delta = Instant.now().until(result.expiry, ChronoUnit.MILLIS)
        val target = delta + SystemClock.elapsedRealtime()

        appWidgetIds.forEach { appWidgetId ->
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.cetus_cycle_layout
            ).apply {
                val display = when (result.state) {
                    State.NIGH -> "night"
                    State.DAY -> "day"
                }
                setTextViewText(R.id.textView, display)
                setChronometer(R.id.chrono, target, null, true)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}