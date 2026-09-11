package com.nerufuyo.hyperisland.ui.screens.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nerufuyo.hyperisland.R
import com.nerufuyo.hyperisland.data.db.AppDatabase
import com.nerufuyo.hyperisland.data.db.MuteProfile
import com.nerufuyo.hyperisland.ui.AppListViewModel
import com.nerufuyo.hyperisland.ui.components.AppListItem
import com.nerufuyo.hyperisland.ui.components.ListOptionCard
import com.nerufuyo.hyperisland.util.isUsageAccessGranted
import com.nerufuyo.hyperisland.util.openUsageAccessSettings
import kotlinx.coroutines.launch
import java.util.UUID

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

/**
 * Create or edit one Mute Profile. profileId == null means "new profile".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(profileId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).muteProfileDao() }
    val appListViewModel: AppListViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    var triggerType by remember { mutableStateOf(MuteProfile.TRIGGER_MANUAL) }
    var scheduleStart by remember { mutableIntStateOf(22 * 60) }
    var scheduleEnd by remember { mutableIntStateOf(7 * 60) }
    var selectedApps by remember { mutableStateOf(emptySet<String>()) }
    var hasUsageAccess by remember { mutableStateOf(isUsageAccessGranted(context)) }
    val apps by appListViewModel.libraryAppsState.collectAsState()

    LaunchedEffect(profileId) {
        if (profileId != null) {
            dao.getById(profileId)?.let { p ->
                name = p.name
                enabled = p.enabled
                triggerType = p.triggerType
                scheduleStart = p.scheduleStartMinutes
                scheduleEnd = p.scheduleEndMinutes
                selectedApps = p.triggerApps.split(",").filter { it.isNotEmpty() }.toSet()
            }
        }
    }

    fun save() {
        if (name.isBlank()) return
        scope.launch {
            dao.upsert(
                MuteProfile(
                    id = profileId ?: UUID.randomUUID().toString(),
                    name = name.trim(),
                    enabled = enabled,
                    triggerType = triggerType,
                    scheduleStartMinutes = scheduleStart,
                    scheduleEndMinutes = scheduleEnd,
                    triggerApps = selectedApps.joinToString(",")
                )
            )
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (profileId == null) R.string.profile_editor_new_title else R.string.profile_editor_edit_title))
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    if (profileId != null) {
                        IconButton(onClick = {
                            scope.launch {
                                dao.delete(profileId)
                                onBack()
                            }
                        }) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.profile_editor_delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.profile_editor_name)) },
                    placeholder = { Text(stringResource(R.string.profile_editor_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.profile_editor_trigger_type),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val options = listOf(
                    Triple(MuteProfile.TRIGGER_MANUAL, R.string.profile_editor_trigger_manual, Icons.Outlined.NotificationsPaused),
                    Triple(MuteProfile.TRIGGER_SCHEDULE, R.string.profile_editor_trigger_schedule, Icons.Outlined.Bedtime),
                    Triple(MuteProfile.TRIGGER_APP_FOREGROUND, R.string.profile_editor_trigger_app, Icons.Outlined.SportsEsports)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, (type, labelRes, icon) ->
                        SegmentedButton(
                            selected = triggerType == type,
                            onClick = { triggerType = type },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            icon = { Icon(icon, contentDescription = null) }
                        ) { Text(stringResource(labelRes)) }
                    }
                }

                Spacer(Modifier.height(16.dp))

                when (triggerType) {
                    MuteProfile.TRIGGER_SCHEDULE -> {
                        ListOptionCard(
                            title = stringResource(R.string.profile_editor_start_time),
                            subtitle = formatMinutes(scheduleStart),
                            icon = Icons.Outlined.Bedtime,
                            shape = RoundedCornerShape(4.dp),
                            onClick = { showTimePicker(context, scheduleStart) { scheduleStart = it } }
                        )
                        Spacer(Modifier.height(2.dp))
                        ListOptionCard(
                            title = stringResource(R.string.profile_editor_end_time),
                            subtitle = formatMinutes(scheduleEnd),
                            icon = Icons.Outlined.Bedtime,
                            shape = RoundedCornerShape(4.dp),
                            onClick = { showTimePicker(context, scheduleEnd) { scheduleEnd = it } }
                        )
                    }
                    MuteProfile.TRIGGER_APP_FOREGROUND -> {
                        if (!hasUsageAccess) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text(
                                    stringResource(R.string.profile_editor_usage_access_needed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                FilledTonalButton(onClick = {
                                    openUsageAccessSettings(context)
                                    hasUsageAccess = isUsageAccessGranted(context)
                                }) {
                                    Text(stringResource(R.string.grant))
                                }
                            }
                        }
                        Text(
                            stringResource(R.string.profile_editor_pick_apps),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    else -> {}
                }
            }

            if (triggerType == MuteProfile.TRIGGER_APP_FOREGROUND) {
                items(apps, key = { it.packageName }) { app ->
                    AppListItem(
                        app = app,
                        checked = selectedApps.contains(app.packageName),
                        onToggle = { checked ->
                            selectedApps = if (checked) selectedApps + app.packageName else selectedApps - app.packageName
                        },
                        onSettingsClick = {}
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                FilledTonalButton(onClick = { save() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.profile_editor_save))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
