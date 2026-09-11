package com.nerufuyo.hyperisland.service

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.widget.RemoteViews
import com.nerufuyo.hyperisland.R
import com.nerufuyo.hyperisland.data.AppPreferences
import com.nerufuyo.hyperisland.data.theme.ThemeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Home-screen widget that cycles the active theme through the installed themes
 * (built-in presets + user-created/imported) on tap. Reuses the exact
 * apply-theme sequence ThemeViewModel uses (setActiveThemeId -> activateTheme ->
 * reload the running NotificationReaderService) so behavior stays identical to
 * switching a theme from within the app.
 */
class ThemeSwitcherWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_CYCLE_THEME = "com.nerufuyo.hyperisland.ACTION_CYCLE_THEME"

        /**
         * Pure "what comes next" step, pulled out so it's testable without Context/Room.
         * ids should already be in the desired cycle order (name-sorted). Returns null
         * only when there are no themes at all.
         */
        internal fun nextThemeId(idsInOrder: List<String>, currentId: String?): String? {
            if (idsInOrder.isEmpty()) return null
            val currentIndex = idsInOrder.indexOf(currentId)
            return idsInOrder[(currentIndex + 1) % idsInOrder.size]
        }

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ThemeSwitcherWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(context, ThemeSwitcherWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                )
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // goAsync() so the coroutine below survives after this method returns -
        // onUpdate itself must return quickly, but Room access is suspend.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentName = currentThemeName(context)
                appWidgetIds.forEach { id ->
                    updateWidget(context, appWidgetManager, id, currentName)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_CYCLE_THEME) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cycleTheme(context)
                val name = currentThemeName(context)
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, ThemeSwitcherWidgetProvider::class.java))
                ids.forEach { id -> updateWidget(context, manager, id, name) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun cycleTheme(context: Context) {
        val preferences = AppPreferences(context)
        val repo = ThemeRepository(context)

        val themes = repo.getAvailableThemes().sortedBy { it.meta.name }
        val currentId = preferences.activeThemeIdFlow.first()
        val nextId = nextThemeId(themes.map { it.id }, currentId) ?: return

        preferences.setActiveThemeId(nextId)
        repo.activateTheme(nextId)

        // Same reload trigger ThemeViewModel.reloadNotificationService() uses.
        context.startService(
            Intent(context, NotificationReaderService::class.java).apply {
                action = NotificationReaderService.ACTION_RELOAD_THEME
            }
        )
    }

    private suspend fun currentThemeName(context: Context): String {
        val preferences = AppPreferences(context)
        val repo = ThemeRepository(context)
        val currentId = preferences.activeThemeIdFlow.first() ?: return context.getString(R.string.widget_theme_switcher_none)
        val theme = repo.getAvailableThemes().find { it.id == currentId }
        return theme?.meta?.name ?: context.getString(R.string.widget_theme_switcher_none)
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int, themeName: String) {
        val views = RemoteViews(context.packageName, R.layout.widget_theme_switcher)
        views.setTextViewText(R.id.widget_theme_name, themeName)

        val clickIntent = Intent(context, ThemeSwitcherWidgetProvider::class.java).apply {
            action = ACTION_CYCLE_THEME
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, widgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_theme_root, pendingIntent)

        manager.updateAppWidget(widgetId, views)
    }
}
