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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionType
import com.example.viewmodel.MainViewModel

@Composable
fun DashboardScreen(viewModel: MainViewModel, onAddTransaction: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    val goals by viewModel.goals.collectAsState()
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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 8.dp).size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    ExpenseCard(
                        modifier = Modifier.weight(1f),
                        amount = totalExpense,
                        formatter = { viewModel.formatAmount(it) }
                    )
                    SavingsCard(
                        modifier = Modifier.weight(1f),
                        goal = goals.firstOrNull(),
                        balance = currentBalance
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Transactions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    TextButton(onClick = { /* Handle see all */ }) {
                        Text("See All", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }
            
            val recentTxs = transactions.take(5)
            if (recentTxs.isEmpty()) {
                item {
                    Text("No transactions yet.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Spacer(modifier = Modifier.height(72.dp))
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6750A4)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Column {
                Text("Welcome back,", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Current Balance", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF21005D))
                Text(
                    text = formatter(balance),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21005D)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("TOTAL INCOME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        Text("+" + formatterNoDecimals(income), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D)) // green-700
                    }
                    Column {
                        Text("TOTAL EXPENSE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        Text("-" + formatterNoDecimals(expense), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) // red-700
                    }
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF6750A4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("↑", color = Color.White, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(modifier: Modifier = Modifier, amount: Double, formatter: (Double) -> String) {
    Card(
        modifier = modifier.height(128.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF2B8B5)),
                contentAlignment = Alignment.Center
            ) {
                Text("↓", color = Color(0xFF601410), fontSize = 16.sp)
            }
            Column {
                Text("Expenses", fontSize = 12.sp, color = Color(0xFF49454F))
                Text(formatter(amount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun SavingsCard(modifier: Modifier = Modifier, goal: com.example.data.Goal?, balance: Double) {
    Card(
        modifier = modifier.height(128.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0BCFF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEADDFF)),
                contentAlignment = Alignment.Center
            ) {
                Text("★", color = Color(0xFF21005D), fontSize = 16.sp)
            }
            Column {
                if (goal != null) {
                    val progress = if (goal.targetAmount > 0) (balance / goal.targetAmount).toFloat() else 0f
                    val percentage = (progress.coerceIn(0f, 1f) * 100).toInt()
                    Text(goal.goalName, fontSize = 12.sp, color = Color(0xFF21005D), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("$percentage%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = Color(0xFF6750A4),
                        trackColor = Color(0xFFEADDFF)
                    )
                } else {
                    Text("Savings Goal", fontSize = 12.sp, color = Color(0xFF21005D))
                    Text("No Goal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = Color(0xFF6750A4),
                        trackColor = Color(0xFFEADDFF)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactTransactionItem(tx: com.example.data.Transaction, formatter: (Double) -> String) {
    val isIncome = tx.type == TransactionType.INCOME
    val sign = if (isIncome) "+" else "-"
    val txColor = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626) // green-600 : red-600
    val iconBg = if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFFEDD5) // green-100 : orange-100

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3EDF7))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isIncome) "↓" else "↑", color = txColor, fontSize = 20.sp)
                }
                Column {
                    Text(tx.category, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(if (isIncome) "Income" else "Expense", fontSize = 10.sp, color = Color(0xFF49454F))
                }
            }
            Text(
                "$sign${formatter(tx.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = txColor
            )
        }
    }
}
