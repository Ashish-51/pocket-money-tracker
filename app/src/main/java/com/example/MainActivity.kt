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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val balance = transactions.sumOf { if (it.type == com.example.data.TransactionType.INCOME) it.amount else -it.amount }

    val items = listOf(
        Screen.Dashboard,
        Screen.History,
        Screen.Stats,
        Screen.Budgets,
        Screen.Search,
        Screen.Settings
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0B1020),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(320.dp)
            ) {
                // Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF111827), Color(0xFF0B1020))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column {
                        val nameToShow = userProfile?.name ?: "User"
                        val parts = nameToShow.trim().split("\\s+".toRegex())
                        val initials = if (parts.size >= 2) {
                            (parts[0].take(1) + parts[1].take(1)).uppercase()
                        } else if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                            parts[0].take(2).uppercase()
                        } else {
                            "US"
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5A8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color(0xFF0B1020),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(nameToShow, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(userProfile?.email ?: "user@example.com", color = Color(0xFFA8B3CF), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Current Balance", color = Color(0xFFA8B3CF), fontSize = 14.sp)
                        Text(viewModel.formatAmountNoDecimals(balance), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label, tint = if (selected) Color.White else Color(0xFFA8B3CF)) },
                        label = { Text(screen.label, color = if (selected) Color.White else Color(0xFFA8B3CF), fontWeight = FontWeight.Medium) },
                        selected = selected,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF00E5A8),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(100.dp)) // Fully rounded selection indicator
                    )
                }
            }
        }
    ) {
        val onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
        NavHost(navController, startDestination = Screen.Dashboard.route) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { onOpenDrawer() },
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
            composable(Screen.History.route) { HistoryScreen(viewModel, onOpenDrawer = { onOpenDrawer() }) }
            composable(Screen.Search.route) { SearchScreen(viewModel, onOpenDrawer = { onOpenDrawer() }) }
            composable(Screen.Stats.route) { StatsScreen(viewModel, onOpenDrawer = { onOpenDrawer() }) }
            composable(Screen.Budgets.route) { BudgetsScreen(viewModel, onOpenDrawer = { onOpenDrawer() }) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel, onOpenDrawer = { onOpenDrawer() }) }
            composable("add_transaction") { AddTransactionScreen(viewModel) { navController.popBackStack() } }
        }
    }
}
