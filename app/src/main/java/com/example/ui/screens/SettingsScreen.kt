package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onOpenDrawer: () -> Unit) {
    val currency by viewModel.currency.collectAsState()
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val email = userProfile?.email ?: ""

    val transactions by viewModel.transactions.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = FintechBackground,
        topBar = { 
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = FintechOnSurface)
                    }
                },
                actions = {
                    NotificationIcon(viewModel = viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FintechBackground)
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FintechSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FintechSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(userProfile?.name ?: "User", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = FintechOnSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(email, fontSize = 14.sp, color = FintechOnSurfaceVariant)
                }
            }

            // Currency Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FintechSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Preferred Currency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setCurrency("USD") },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (currency == "USD") FintechPrimary.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = if (currency == "USD") FintechPrimary else FintechOnSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (currency == "USD") FintechPrimary else FintechOutline.copy(alpha = 0.3f)
                            )
                        ) {
                            Text("USD ($)", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setCurrency("INR") },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (currency == "INR") FintechPrimary.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = if (currency == "INR") FintechPrimary else FintechOnSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (currency == "INR") FintechPrimary else FintechOutline.copy(alpha = 0.3f)
                            )
                        ) {
                            Text("INR (₹)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Data Export Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FintechSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Data Management",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val data = viewModel.getExportDataString(transactions)
                            val sendIntent: android.content.Intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, data)
                                type = "text/csv"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "Export Transactions")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechPrimary.copy(alpha = 0.1f), contentColor = FintechPrimary),
                        elevation = null
                    ) {
                        Text("Export Data (CSV)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FintechError.copy(alpha = 0.1f), contentColor = FintechError)
            ) {
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
