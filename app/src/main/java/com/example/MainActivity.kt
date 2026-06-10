package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthState
import com.example.viewmodel.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(com.example.BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(com.example.BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(com.example.BuildConfig.FIREBASE_PROJECT_ID)
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                PocketMoneyApp()
            }
        }
    }
}

@Composable
fun PocketMoneyApp() {
    val viewModel: MainViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                // Loading
            }
        }
        is AuthState.Unauthenticated -> {
            AuthScreen(viewModel = viewModel)
        }
        is AuthState.Authenticated -> {
            MainNavigation(viewModel = viewModel)
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home)
    object History : Screen("history", "History", Icons.Filled.List)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object Stats : Screen("stats", "Stats", Icons.Filled.PieChart)
    object Budgets : Screen("budgets", "Subscriptions", Icons.Filled.Star)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun MainNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard,
        Screen.Search,
        Screen.Stats,
        Screen.Budgets,
        Screen.Settings
    )

    Scaffold(
        containerColor = com.example.ui.theme.FintechBackground,
        bottomBar = {
            NavigationBar(
                containerColor = com.example.ui.theme.FintechSurface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.example.ui.theme.FintechOnPrimary,
                            selectedTextColor = com.example.ui.theme.FintechPrimary,
                            unselectedIconColor = com.example.ui.theme.FintechOnSurfaceVariant,
                            unselectedTextColor = com.example.ui.theme.FintechOnSurfaceVariant,
                            indicatorColor = com.example.ui.theme.FintechPrimary
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Dashboard.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    viewModel = viewModel,
                    onSeeAll = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddTransaction = { navController.navigate("add_transaction") }
                ) 
            }
            composable(Screen.History.route) { HistoryScreen(viewModel) }
            composable(Screen.Search.route) { SearchScreen(viewModel) }
            composable(Screen.Stats.route) { StatsScreen(viewModel) }
            composable(Screen.Budgets.route) { BudgetsScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
            composable("add_transaction") { AddTransactionScreen(viewModel) { navController.popBackStack() } }
        }
    }
}
