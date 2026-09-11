package com.nerufuyo.hyperisland.ui.screens.settings

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nerufuyo.hyperisland.R
import com.nerufuyo.hyperisland.data.AppPreferences
import com.nerufuyo.hyperisland.ui.components.ListOptionCard
import kotlinx.coroutines.launch

private fun formatMinutes(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return String.format("%02d:%02d", h, m)
}

private fun showTimePicker(context: android.content.Context, initialMinutes: Int, onPicked: (Int) -> Unit) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(hour * 60 + minute) },
        initialMinutes / 60,
        initialMinutes % 60,
        true
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DndSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val isDndModeEnabled by prefs.isDndModeEnabledFlow.collectAsState(initial = false)
    val autoDetectDnd by prefs.autoDetectDndFlow.collectAsState(initial = false)
    val dndScheduleEnabled by prefs.dndScheduleEnabledFlow.collectAsState(initial = false)
    val dndScheduleStart by prefs.dndScheduleStartMinutesFlow.collectAsState(
        initial = AppPreferences.DEFAULT_DND_SCHEDULE_START_MINUTES
    )
    val dndScheduleEnd by prefs.dndScheduleEndMinutesFlow.collectAsState(
        initial = AppPreferences.DEFAULT_DND_SCHEDULE_END_MINUTES
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dnd_mode_title)) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.dnd_mode_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
            )

            ListOptionCard(
                title = stringResource(R.string.dnd_auto_detect),
                subtitle = stringResource(R.string.dnd_auto_detect_desc),
                icon = Icons.Outlined.DoNotDisturbOn,
                shape = RoundedCornerShape(24.dp, 24.dp, 4.dp, 4.dp),
                onClick = {
                    scope.launch { prefs.setAutoDetectDnd(!autoDetectDnd) }
                },
                trailingContent = {
                    Switch(checked = autoDetectDnd, onCheckedChange = {
                        scope.launch { prefs.setAutoDetectDnd(it) }
                    })
                }
            )

            Spacer(Modifier.height(2.dp))

            ListOptionCard(
                title = stringResource(R.string.dnd_manual_toggle),
                subtitle = stringResource(R.string.dnd_manual_toggle_desc),
                icon = Icons.Outlined.NotificationsPaused,
                shape = RoundedCornerShape(4.dp, 4.dp, if (dndScheduleEnabled) 4.dp else 24.dp, if (dndScheduleEnabled) 4.dp else 24.dp),
                onClick = {
                    scope.launch { prefs.setDndModeEnabled(!isDndModeEnabled) }
                },
                trailingContent = {
                    Switch(checked = isDndModeEnabled, onCheckedChange = {
                        scope.launch { prefs.setDndModeEnabled(it) }
                    })
                }
            )

            Spacer(Modifier.height(2.dp))

            ListOptionCard(
                title = stringResource(R.string.dnd_schedule),
                subtitle = stringResource(R.string.dnd_schedule_desc),
                icon = Icons.Outlined.Bedtime,
                shape = RoundedCornerShape(4.dp, 4.dp, if (dndScheduleEnabled) 4.dp else 24.dp, if (dndScheduleEnabled) 4.dp else 24.dp),
                onClick = {
                    scope.launch { prefs.setDndScheduleEnabled(!dndScheduleEnabled) }
                },
                trailingContent = {
                    Switch(checked = dndScheduleEnabled, onCheckedChange = {
                        scope.launch { prefs.setDndScheduleEnabled(it) }
                    })
                }
            )

            AnimatedVisibility(visible = dndScheduleEnabled) {
                Column {
                    Spacer(Modifier.height(2.dp))
                    ListOptionCard(
                        title = stringResource(R.string.dnd_schedule_start),
                        subtitle = formatMinutes(dndScheduleStart),
                        icon = Icons.Outlined.Bedtime,
                        shape = RoundedCornerShape(4.dp),
                        onClick = {
                            showTimePicker(context, dndScheduleStart) { minutes ->
                                scope.launch { prefs.setDndScheduleStartMinutes(minutes) }
                            }
                        }
                    )
                    Spacer(Modifier.height(2.dp))
                    ListOptionCard(
                        title = stringResource(R.string.dnd_schedule_end),
                        subtitle = formatMinutes(dndScheduleEnd),
                        icon = Icons.Outlined.Bedtime,
                        shape = RoundedCornerShape(4.dp, 4.dp, 24.dp, 24.dp),
                        onClick = {
                            showTimePicker(context, dndScheduleEnd) { minutes ->
                                scope.launch { prefs.setDndScheduleEndMinutes(minutes) }
                            }
                        }
                    )
                }
            }
        }
    }
}
