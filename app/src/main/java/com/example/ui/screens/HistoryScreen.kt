package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel, onOpenDrawer: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = FintechSurface,
            titleContentColor = FintechOnSurface,
            textContentColor = FintechOnSurfaceVariant,
            title = { Text("Reset All Transactions", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all transactions? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllTransactions()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechError.copy(alpha = 0.1f), contentColor = FintechError),
                    elevation = null
                ) {
                    Text("Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = FintechOnSurfaceVariant)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = FintechBackground,
        topBar = { 
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = FintechOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FintechBackground),
                actions = {
                    if (transactions.isNotEmpty()) {
                        IconButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Reset All", tint = FintechError.copy(alpha = 0.8f))
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = FintechOnSurface)
                    }
                }
            ) 
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No transactions found.", color = FintechOutline, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(transactions) { tx ->
                    TransactionItem(tx, formatter = { viewModel.formatAmount(it) }, onDelete = { viewModel.deleteTransaction(tx.transactionId) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, formatter: (Double) -> String, onDelete: () -> Unit) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = transaction.timestamp.toDate().let { dateFormatter.format(it) }

    val isIncome = transaction.type == TransactionType.INCOME
    val sign = if (isIncome) "+" else "-"
    val txColor = if (isIncome) FintechIncome else FintechError
    val iconBg = txColor.copy(alpha = 0.15f)

    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FintechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isIncome) "↑" else "↓", color = txColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = transaction.category, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = FintechOnSurface)
                    if (transaction.note.isNotBlank()) {
                        Text(text = transaction.note, fontSize = 12.sp, color = FintechOnSurfaceVariant, maxLines = 1)
                    }
                    Text(text = "$dateStr • ${transaction.paymentMethod}", fontSize = 12.sp, color = FintechOutline)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign${formatter(transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = txColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = FintechError.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
