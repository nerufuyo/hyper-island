package com.nerufuyo.hyperisland.ui.screens.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.nerufuyo.hyperisland.R
import com.nerufuyo.hyperisland.data.AppPreferences
import com.nerufuyo.hyperisland.data.db.AppDatabase
import com.nerufuyo.hyperisland.data.db.MuteProfile
import com.nerufuyo.hyperisland.ui.components.EmptyState
import com.nerufuyo.hyperisland.ui.components.ListOptionCard
import kotlinx.coroutines.launch

/**
 * android:configure target for ProfileToggleWidgetProvider - picks which Mute Profile this
 * widget instance toggles. Standard Android widget-configuration contract: default to
 * RESULT_CANCELED up front, only RESULT_OK once a profile is actually chosen and saved.
 */
class ProfileToggleWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val prefs = AppPreferences(applicationContext)

        setContent {
            val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colorScheme) {
                ProfilePickerScreen(
                    onPick = { profile ->
                        lifecycleScope.launch {
                            prefs.setWidgetProfileId(appWidgetId, profile.id)
                            val manager = AppWidgetManager.getInstance(applicationContext)
                            com.nerufuyo.hyperisland.service.ProfileToggleWidgetProvider.updateWidget(
                                applicationContext, manager, appWidgetId
                            )
                            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePickerScreen(onPick: (MuteProfile) -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).muteProfileDao() }
    val profiles by dao.getAllFlow().collectAsState(initial = null)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.profile_widget_pick_title)) }) }
    ) { padding ->
        when {
            profiles == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            profiles!!.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.mute_profiles_empty),
                    description = stringResource(R.string.profile_widget_create_first),
                    icon = Icons.Outlined.Tune
                )
            }
            else -> LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(profiles!!, key = { it.id }) { profile ->
                    val triggerLabel = stringResource(
                        when (profile.triggerType) {
                            MuteProfile.TRIGGER_SCHEDULE -> R.string.profile_editor_trigger_schedule
                            MuteProfile.TRIGGER_APP_FOREGROUND -> R.string.profile_editor_trigger_app
                            MuteProfile.TRIGGER_BLUETOOTH -> R.string.profile_editor_trigger_bluetooth
                            MuteProfile.TRIGGER_LOCATION -> R.string.profile_editor_trigger_location
                            else -> R.string.profile_editor_trigger_manual
                        }
                    )
                    ListOptionCard(
                        title = profile.name,
                        subtitle = triggerLabel,
                        icon = Icons.Outlined.Tune,
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onPick(profile) }
                    )
                }
            }
        }
    }
}
