package com.example.finsight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.finsight.presentation.components.FinsightBottomBar
import com.example.finsight.presentation.navigation.NavGraph
import com.example.finsight.presentation.navigation.Screen
import com.example.finsight.presentation.navigation.bottomNavItems
import com.example.finsight.presentation.screens.login.LoginScreen
import com.example.finsight.presentation.screens.settings.SettingsDataStore
import com.example.finsight.presentation.theme.FinsightTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore
) : ViewModel() {
    val darkMode: StateFlow<Boolean> = settingsDataStore.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userName: StateFlow<String> = settingsDataStore.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val darkMode by mainViewModel.darkMode.collectAsState()
            val userName by mainViewModel.userName.collectAsState()

            FinsightTheme(darkTheme = darkMode) {
                // Show login if userName is empty (not yet set)
                val isLoggedIn = userName.isNotBlank()

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { /* userName is saved, state updates reactively */ }
                    )
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar) {
                                FinsightBottomBar(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        // FIX: Pass innerPadding so content isn't hidden behind nav bar
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        )
                    }
                }
            }
        }
    }
}
