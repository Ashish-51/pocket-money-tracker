package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.TransactionType
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val total = totalIncome + totalExpense
    
    val incomeSweep = if (total > 0) (totalIncome / total) * 360f else 0f
    val expenseSweep = if (total > 0) (totalExpense / total) * 360f else 0f

    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistics") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Income vs Expense", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            
            if (total == 0.0) {
                Text("Not enough data to display statistics.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawArc(
                        color = incomeColor,
                        startAngle = 0f,
                        sweepAngle = incomeSweep.toFloat(),
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    drawArc(
                        color = expenseColor,
                        startAngle = incomeSweep.toFloat(),
                        sweepAngle = expenseSweep.toFloat(),
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(16.dp), color = incomeColor, shape = MaterialTheme.shapes.small) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Income")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(16.dp), color = expenseColor, shape = MaterialTheme.shapes.small) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Expense")
                    }
                }
            }
        }
    }
}
