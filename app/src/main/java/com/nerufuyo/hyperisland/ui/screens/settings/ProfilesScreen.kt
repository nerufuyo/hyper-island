package com.nerufuyo.hyperisland.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nerufuyo.hyperisland.R
import com.nerufuyo.hyperisland.data.db.AppDatabase
import com.nerufuyo.hyperisland.data.db.MuteProfile
import com.nerufuyo.hyperisland.ui.components.EmptyState
import kotlinx.coroutines.launch

private fun iconFor(triggerType: String): ImageVector = when (triggerType) {
    MuteProfile.TRIGGER_SCHEDULE -> Icons.Outlined.Bedtime
    MuteProfile.TRIGGER_APP_FOREGROUND -> Icons.Outlined.SportsEsports
    MuteProfile.TRIGGER_BLUETOOTH -> Icons.Outlined.Bluetooth
    MuteProfile.TRIGGER_LOCATION -> Icons.Outlined.LocationOn
    else -> Icons.Outlined.NotificationsPaused
}

private fun subtitleFor(profile: MuteProfile): String = when (profile.triggerType) {
    MuteProfile.TRIGGER_SCHEDULE -> {
        fun fmt(m: Int) = String.format("%02d:%02d", m / 60, m % 60)
        "${fmt(profile.scheduleStartMinutes)} - ${fmt(profile.scheduleEndMinutes)}"
    }
    MuteProfile.TRIGGER_APP_FOREGROUND -> {
        val count = profile.triggerApps.split(",").filter { it.isNotEmpty() }.size
        "$count app(s)"
    }
    MuteProfile.TRIGGER_BLUETOOTH -> profile.triggerBluetoothAddress.ifEmpty { "No device set" }
    MuteProfile.TRIGGER_LOCATION -> profile.triggerLocationName.ifEmpty { "No place set" }
    else -> "Manual"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(onBack: () -> Unit, onEditProfile: (String?) -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).muteProfileDao() }
    val profiles by dao.getAllFlow().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mute_profiles_title)) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    FilledTonalIconButton(onClick = { onEditProfile(null) }) {
                        Icon(Icons.Filled.Add, stringResource(R.string.mute_profiles_add))
                    }
                }
            )
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(R.string.mute_profiles_empty),
                    description = stringResource(R.string.mute_profiles_empty_desc),
                    icon = Icons.Outlined.Tune
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
                items(profiles, key = { it.id }) { profile ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        onClick = { onEditProfile(profile.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(iconFor(profile.triggerType), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    subtitleFor(profile),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = profile.enabled,
                                onCheckedChange = { checked ->
                                    scope.launch { dao.upsert(profile.copy(enabled = checked)) }
                                }
                            )
                        }
                    }
                }
                item { Spacer(Modifier.size(72.dp)) }
            }
        }
    }
}
