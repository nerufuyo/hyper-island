package com.nerufuyo.hyperisland.ui.screens.settings

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.nerufuyo.hyperisland.util.getBondedBluetoothDevices
import com.nerufuyo.hyperisland.util.getCurrentLocation
import com.nerufuyo.hyperisland.util.isBackgroundLocationGranted
import com.nerufuyo.hyperisland.util.isBluetoothConnectGranted
import com.nerufuyo.hyperisland.util.isFineLocationGranted
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
    var selectedPriorityApps by remember { mutableStateOf(emptySet<String>()) }
    var showPriorityPicker by remember { mutableStateOf(false) }
    var hasUsageAccess by remember { mutableStateOf(isUsageAccessGranted(context)) }
    var hasBluetoothAccess by remember { mutableStateOf(isBluetoothConnectGranted(context)) }
    var selectedBluetoothAddress by remember { mutableStateOf("") }
    var hasFineLocation by remember { mutableStateOf(isFineLocationGranted(context)) }
    var hasBackgroundLocation by remember { mutableStateOf(isBackgroundLocationGranted(context)) }
    var locationName by remember { mutableStateOf("") }
    var triggerLatitude by remember { mutableDoubleStateOf(0.0) }
    var triggerLongitude by remember { mutableDoubleStateOf(0.0) }
    var triggerRadiusMeters by remember { mutableIntStateOf(150) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    val apps by appListViewModel.libraryAppsState.collectAsState()
    val bluetoothDevices = remember(hasBluetoothAccess) { getBondedBluetoothDevices(context) }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasBluetoothAccess = granted }
    )
    val fineLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasFineLocation = granted }
    )
    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasBackgroundLocation = granted }
    )

    LaunchedEffect(profileId) {
        if (profileId != null) {
            dao.getById(profileId)?.let { p ->
                name = p.name
                enabled = p.enabled
                triggerType = p.triggerType
                scheduleStart = p.scheduleStartMinutes
                scheduleEnd = p.scheduleEndMinutes
                selectedApps = p.triggerApps.split(",").filter { it.isNotEmpty() }.toSet()
                selectedPriorityApps = p.priorityApps.split(",").filter { it.isNotEmpty() }.toSet()
                selectedBluetoothAddress = p.triggerBluetoothAddress
                locationName = p.triggerLocationName
                triggerLatitude = p.triggerLatitude
                triggerLongitude = p.triggerLongitude
                triggerRadiusMeters = p.triggerRadiusMeters.toInt()
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
                    triggerApps = selectedApps.joinToString(","),
                    priorityApps = selectedPriorityApps.joinToString(","),
                    triggerBluetoothAddress = selectedBluetoothAddress,
                    triggerLocationName = locationName.trim(),
                    triggerLatitude = triggerLatitude,
                    triggerLongitude = triggerLongitude,
                    triggerRadiusMeters = triggerRadiusMeters.toFloat()
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
                    Triple(MuteProfile.TRIGGER_APP_FOREGROUND, R.string.profile_editor_trigger_app, Icons.Outlined.SportsEsports),
                    Triple(MuteProfile.TRIGGER_BLUETOOTH, R.string.profile_editor_trigger_bluetooth, Icons.Outlined.Bluetooth),
                    Triple(MuteProfile.TRIGGER_LOCATION, R.string.profile_editor_trigger_location, Icons.Outlined.LocationOn)
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
                    MuteProfile.TRIGGER_BLUETOOTH -> {
                        if (!hasBluetoothAccess) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text(
                                    stringResource(R.string.profile_editor_bluetooth_access_needed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                FilledTonalButton(onClick = {
                                    bluetoothPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
                                }) {
                                    Text(stringResource(R.string.grant))
                                }
                            }
                        } else {
                            Text(
                                stringResource(R.string.profile_editor_pick_device),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    MuteProfile.TRIGGER_LOCATION -> {
                        if (!hasFineLocation) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text(
                                    stringResource(R.string.profile_editor_location_access_needed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                FilledTonalButton(onClick = {
                                    fineLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                }) {
                                    Text(stringResource(R.string.grant))
                                }
                            }
                        } else if (!hasBackgroundLocation) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Text(
                                    stringResource(R.string.profile_editor_background_location_needed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                FilledTonalButton(onClick = {
                                    backgroundLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                }) {
                                    Text(stringResource(R.string.grant))
                                }
                            }
                        } else {
                            FilledTonalButton(
                                onClick = {
                                    isFetchingLocation = true
                                    scope.launch {
                                        val location = getCurrentLocation(context)
                                        if (location != null) {
                                            triggerLatitude = location.latitude
                                            triggerLongitude = location.longitude
                                        }
                                        isFetchingLocation = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isFetchingLocation) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                } else {
                                    Icon(Icons.Outlined.LocationOn, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.profile_editor_use_current_location))
                                }
                            }

                            if (triggerLatitude != 0.0 || triggerLongitude != 0.0) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(
                                        R.string.profile_editor_location_captured,
                                        triggerLatitude,
                                        triggerLongitude
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = locationName,
                                    onValueChange = { locationName = it },
                                    label = { Text(stringResource(R.string.profile_editor_location_name)) },
                                    placeholder = { Text(stringResource(R.string.profile_editor_location_name_placeholder)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.profile_editor_radius),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Row {
                                    listOf(100, 150, 300).forEach { radius ->
                                        FilterChip(
                                            selected = triggerRadiusMeters == radius,
                                            onClick = { triggerRadiusMeters = radius },
                                            label = { Text("${radius}m") },
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
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

            if (triggerType == MuteProfile.TRIGGER_BLUETOOTH && hasBluetoothAccess) {
                if (bluetoothDevices.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.profile_editor_no_devices),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(bluetoothDevices, key = { it.first }) { (address, deviceName) ->
                    ListOptionCard(
                        title = deviceName,
                        subtitle = address,
                        icon = Icons.Outlined.Bluetooth,
                        shape = RoundedCornerShape(4.dp),
                        onClick = { selectedBluetoothAddress = address },
                        trailingContent = {
                            RadioButton(
                                selected = selectedBluetoothAddress == address,
                                onClick = { selectedBluetoothAddress = address }
                            )
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                ListOptionCard(
                    title = stringResource(R.string.profile_editor_priority_apps),
                    subtitle = if (selectedPriorityApps.isEmpty()) {
                        stringResource(R.string.profile_editor_priority_apps_none)
                    } else {
                        stringResource(R.string.profile_editor_priority_apps_count, selectedPriorityApps.size)
                    },
                    icon = Icons.Outlined.Shield,
                    shape = RoundedCornerShape(16.dp),
                    onClick = { showPriorityPicker = !showPriorityPicker }
                )
            }

            if (showPriorityPicker) {
                items(apps, key = { "priority_${it.packageName}" }) { app ->
                    AppListItem(
                        app = app,
                        checked = selectedPriorityApps.contains(app.packageName),
                        onToggle = { checked ->
                            selectedPriorityApps = if (checked) {
                                selectedPriorityApps + app.packageName
                            } else {
                                selectedPriorityApps - app.packageName
                            }
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
