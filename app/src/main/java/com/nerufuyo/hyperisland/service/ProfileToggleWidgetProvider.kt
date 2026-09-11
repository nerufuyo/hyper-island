package com.nerufuyo.hyperisland.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nerufuyo.hyperisland.R
import com.nerufuyo.hyperisland.data.AppPreferences
import com.nerufuyo.hyperisland.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * One widget instance = one Mute Profile (picked via ProfileToggleWidgetConfigActivity when
 * the widget is placed) - like adding an iOS Focus toggle to your home screen. Tapping flips
 * that profile's enabled flag; NotificationReaderService already reacts to MuteProfileDao
 * changes live, so no reload trigger is needed here (unlike the theme-switcher widget, which
 * has to explicitly poke NotificationReaderService.ACTION_RELOAD_THEME).
 */
class ProfileToggleWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_PROFILE = "com.nerufuyo.hyperisland.ACTION_TOGGLE_PROFILE"

        /** Called by ProfileToggleWidgetConfigActivity right after saving the mapping. */
        suspend fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val prefs = AppPreferences(context)
            val dao = AppDatabase.getDatabase(context).muteProfileDao()
            val profileId = prefs.getWidgetProfileId(widgetId)
            val profile = profileId?.let { dao.getById(it) }

            val views = RemoteViews(context.packageName, R.layout.widget_profile_toggle)
            if (profile == null) {
                views.setTextViewText(R.id.widget_profile_name, context.getString(R.string.profile_widget_not_configured))
                views.setTextViewText(R.id.widget_profile_status, "")
            } else {
                views.setTextViewText(R.id.widget_profile_name, profile.name)
                views.setTextViewText(
                    R.id.widget_profile_status,
                    context.getString(if (profile.enabled) R.string.profile_widget_status_on else R.string.profile_widget_status_off)
                )
            }

            val clickIntent = Intent(context, ProfileToggleWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_PROFILE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, widgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_profile_root, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = AppPreferences(context)
                appWidgetIds.forEach { id -> prefs.clearWidgetProfileId(id) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_TOGGLE_PROFILE) return
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        if (widgetId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = AppPreferences(context)
                val dao = AppDatabase.getDatabase(context).muteProfileDao()
                val profileId = prefs.getWidgetProfileId(widgetId)
                val profile = profileId?.let { dao.getById(it) }
                if (profile != null) {
                    dao.upsert(profile.copy(enabled = !profile.enabled))
                }
                updateWidget(context, AppWidgetManager.getInstance(context), widgetId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
