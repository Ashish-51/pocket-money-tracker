package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionType
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, onOpenDrawer: () -> Unit, onSeeAll: () -> Unit, onAddTransaction: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    val recurring by viewModel.recurringTransactions.collectAsState()
    val userProfile by viewModel.currentUserProfile.collectAsState()
    
    val nameToShow = userProfile?.name ?: "User"
    val parts = nameToShow.trim().split("\\s+".toRegex())
    val initials = if (parts.size >= 2) {
        (parts[0].take(1) + parts[1].take(1)).uppercase()
    } else if (parts.isNotEmpty() && parts[0].isNotBlank()) {
        parts[0].take(2).uppercase()
    } else {
        "US"
    }

    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val currentBalance = totalIncome - totalExpense

    Scaffold(
        containerColor = FintechBackground,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = FintechOnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = FintechOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FintechBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = FintechPrimary,
                contentColor = FintechOnPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 8.dp).size(64.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                HeaderSection(name = nameToShow, initials = initials)
            }
            item {
                BalanceCard(
                    balance = currentBalance,
                    income = totalIncome,
                    expense = totalExpense,
                    formatter = { viewModel.formatAmount(it) },
                    formatterNoDecimals = { viewModel.formatAmountNoDecimals(it) }
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Transactions",
                        value = transactions.size.toString(),
                        icon = "📋",
                        iconColor = FintechPrimary,
                        bgColor = FintechSurface
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Subscriptions",
                        value = recurring.size.toString(),
                        icon = "🔁",
                        iconColor = FintechSecondary,
                        bgColor = FintechSecondary.copy(alpha = 0.1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Transactions", fontWeight = FontWeight.Bold, color = FintechOnSurface, fontSize = 18.sp)
                    TextButton(onClick = onSeeAll) {
                        Text("See All", fontWeight = FontWeight.SemiBold, color = FintechPrimary, fontSize = 14.sp)
                    }
                }
            }
            
            val recentTxs = transactions.take(5)
            if (recentTxs.isEmpty()) {
                item {
                    Text("No transactions yet.", modifier = Modifier.padding(16.dp), color = FintechOutline)
                }
            } else {
                items(recentTxs.size) { index ->
                    CompactTransactionItem(
                        tx = recentTxs[index],
                        formatter = { viewModel.formatAmount(it) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun HeaderSection(name: String, initials: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(FintechPrimary, FintechSecondary))),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = FintechOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Column {
                Text("Welcome back,", fontSize = 14.sp, color = FintechOnSurfaceVariant)
                Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FintechOnSurface)
            }
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    formatter: (Double) -> String,
    formatterNoDecimals: (Double) -> String
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(FintechSurfaceVariant, FintechSurface)
    )

    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(gradientBrush).border(1.dp, FintechOutline.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Current Balance", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = FintechOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatter(balance),
                        style = MaterialTheme.typography.displayLarge.copy(color = FintechOnSurface),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text("INCOME", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FintechOutline)
                            Text("+" + formatterNoDecimals(income), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FintechIncome)
                        }
                        Column {
                            Text("EXPENSE", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FintechOutline)
                            Text("-" + formatterNoDecimals(expense), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FintechError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: String, iconColor: Color, bgColor: Color) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, color = iconColor, fontSize = 20.sp)
            }
            Column {
                Text(title, fontSize = 14.sp, color = FintechOnSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CompactTransactionItem(tx: com.example.data.Transaction, formatter: (Double) -> String) {
    val isIncome = tx.type == TransactionType.INCOME
    val sign = if (isIncome) "+" else "-"
    val txColor = if (isIncome) FintechIncome else FintechError
    val iconBg = txColor.copy(alpha = 0.15f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FintechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isIncome) "↑" else "↓", color = txColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(tx.category, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = FintechOnSurface)
                    Text(tx.paymentMethod, fontSize = 13.sp, color = FintechOnSurfaceVariant)
                }
            }
            Text(
                "$sign${formatter(tx.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = txColor
            )
        }
    }
}
