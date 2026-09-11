package com.nerufuyo.hyperisland.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import com.nerufuyo.hyperisland.data.AppPreferences
import com.nerufuyo.hyperisland.ui.AppListViewModel
import com.nerufuyo.hyperisland.ui.components.AppListItem
import com.nerufuyo.hyperisland.ui.components.ListOptionCard
import com.nerufuyo.hyperisland.util.isUsageAccessGranted
import com.nerufuyo.hyperisland.util.openUsageAccessSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val appListViewModel: AppListViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val focusModeEnabled by prefs.focusModeEnabledFlow.collectAsState(initial = false)
    val focusModeApps by prefs.focusModeAppsFlow.collectAsState(initial = emptySet())
    val apps by appListViewModel.libraryAppsState.collectAsState()

    // Usage Access has no runtime dialog; re-check whenever the screen is shown (e.g. after
    // coming back from Settings) rather than only once.
    var hasUsageAccess by remember { mutableStateOf(isUsageAccessGranted(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.focus_mode_title)) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.focus_mode_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp)
            )

            if (!hasUsageAccess) {
                ListOptionCard(
                    title = stringResource(R.string.focus_mode_grant_usage_access),
                    subtitle = stringResource(R.string.focus_mode_grant_usage_access_desc),
                    icon = Icons.Outlined.SportsEsports,
                    shape = RoundedCornerShape(16.dp),
                    onClick = { openUsageAccessSettings(context) },
                    trailingContent = {
                        FilledTonalButton(onClick = {
                            openUsageAccessSettings(context)
                            hasUsageAccess = isUsageAccessGranted(context)
                        }) { Text(stringResource(R.string.grant)) }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            ListOptionCard(
                title = stringResource(R.string.focus_mode_toggle),
                subtitle = stringResource(R.string.focus_mode_toggle_desc),
                icon = Icons.Outlined.SportsEsports,
                shape = RoundedCornerShape(16.dp),
                onClick = { scope.launch { prefs.setFocusModeEnabled(!focusModeEnabled) } },
                trailingContent = {
                    Switch(
                        checked = focusModeEnabled && hasUsageAccess,
                        enabled = hasUsageAccess,
                        onCheckedChange = { scope.launch { prefs.setFocusModeEnabled(it) } }
                    )
                }
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.focus_mode_pick_apps),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )

            LazyColumn {
                items(apps, key = { it.packageName }) { app ->
                    AppListItem(
                        app = app,
                        checked = focusModeApps.contains(app.packageName),
                        onToggle = { checked ->
                            scope.launch { prefs.toggleFocusModeApp(app.packageName, checked) }
                        },
                        onSettingsClick = {}
                    )
                }
            }
        }
    }
}
