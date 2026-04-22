package com.example.finsight.presentation.screens.settings

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finsight.presentation.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val CURRENCY_KEY = stringPreferencesKey("currency")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE_KEY] ?: false }
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME_KEY] ?: "" }
    val currency: Flow<String> = context.dataStore.data.map { it[CURRENCY_KEY] ?: "INR" }

    suspend fun setDarkMode(value: Boolean) = context.dataStore.edit { it[DARK_MODE_KEY] = value }
    suspend fun setUserName(value: String) = context.dataStore.edit { it[USER_NAME_KEY] = value }
    suspend fun setCurrency(value: String) = context.dataStore.edit { it[CURRENCY_KEY] = value }
}

data class SettingsUiState(
    val darkMode: Boolean = false,
    val userName: String = "User",
    val currency: String = "INR"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        dataStore.darkMode, dataStore.userName, dataStore.currency
    ) { dark, name, curr -> SettingsUiState(dark, name, curr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setDarkMode(value: Boolean) = viewModelScope.launch { dataStore.setDarkMode(value) }
    fun setUserName(value: String) = viewModelScope.launch { dataStore.setUserName(value) }
    fun setCurrency(value: String) = viewModelScope.launch { dataStore.setCurrency(value) }
    fun signOut() = viewModelScope.launch { dataStore.setUserName("") }
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Your name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameInput.isNotBlank()) viewModel.setUserName(nameInput)
                    showNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Finsight") },
            text = {
                Column {
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Finsight is your personal finance companion. Track transactions, set goals, and gain insights into your spending habits.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showCurrencyDialog) {
        val currencies = listOf(
            "USD" to "US Dollar ($)",
            "EUR" to "Euro (€)",
            "GBP" to "British Pound (£)",
            "JPY" to "Japanese Yen (¥)",
            "INR" to "Indian Rupee (₹)"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    currencies.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(code)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            if (state.currency == code) {
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 52.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Filled.ArrowBack, null)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(8.dp))

        // Profile
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = state.userName.firstOrNull()?.uppercase() ?: "U"
                    Text(
                        text = initial.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        fontSize = 24.sp
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.userName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text("Personal Finance Tracker", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { nameInput = state.userName; showNameDialog = true }) {
                    Icon(Icons.Filled.Edit, null, tint = Primary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Appearance section
        SettingsSection("Appearance") {
            SettingsToggleItem(
                icon = Icons.Filled.DarkMode,
                iconColor = Color(0xFF7C6FCD),
                title = "Dark Mode",
                subtitle = "Switch to dark theme",
                checked = state.darkMode,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Notifications section
        SettingsSection("Preferences") {
            val currencyLabel = when(state.currency) {
                "USD" -> "US Dollar ($)"
                "EUR" -> "Euro (€)"
                "GBP" -> "British Pound (£)"
                "JPY" -> "Japanese Yen (¥)"
                "INR" -> "Indian Rupee (₹)"
                else -> state.currency
            }
            SettingsClickItem(
                icon = Icons.Filled.AttachMoney,
                iconColor = Color(0xFF4CAF50),
                title = "Currency",
                subtitle = currencyLabel,
                onClick = { showCurrencyDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            SettingsClickItem(
                icon = Icons.Filled.Notifications,
                iconColor = Color(0xFFFF9800),
                title = "Reminders",
                subtitle = "Daily spending reminders",
                onClick = {}
            )
        }

        Spacer(Modifier.height(16.dp))

        // About section
        SettingsSection("About") {
            SettingsClickItem(
                icon = Icons.Filled.Info,
                iconColor = Color(0xFF2196F3),
                title = "About Finsight",
                subtitle = "Version 1.0.0",
                onClick = { showAboutDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            SettingsClickItem(
                icon = Icons.Filled.Star,
                iconColor = Color(0xFFFFCA28),
                title = "Rate the App",
                subtitle = "Share your feedback",
                onClick = {}
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            SettingsClickItem(
                icon = Icons.Filled.Share,
                iconColor = Color(0xFF00BCD4),
                title = "Share Finsight",
                subtitle = "Tell your friends",
                onClick = {}
            )
        }

        Spacer(Modifier.height(16.dp))

        // Account section
        SettingsSection("Account") {
            SettingsClickItem(
                icon = Icons.Filled.Logout,
                iconColor = Color(0xFFEF5350),
                title = "Sign Out",
                subtitle = "Return to login screen",
                onClick = { viewModel.signOut() }
            )
        }

        Spacer(Modifier.height(40.dp))

        // Footer
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💙", fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Jyotishmaan Deka @2026",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary)
        )
    }
}

@Composable
fun SettingsClickItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
