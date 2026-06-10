package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionType
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MainViewModel, onOpenDrawer: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    
    val dateFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    
    val availableMonths = remember(transactions) {
        val months = transactions.map { dateFormatter.format(it.timestamp.toDate()) }.distinct()
        listOf("All Time") + months
    }
    
    var selectedPeriod by remember { mutableStateOf("All Time") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(availableMonths) {
        if (!availableMonths.contains(selectedPeriod)) {
            selectedPeriod = "All Time"
        }
    }

    val filteredTransactions = remember(transactions, selectedPeriod) {
        if (selectedPeriod == "All Time") {
            transactions
        } else {
            transactions.filter { dateFormatter.format(it.timestamp.toDate()) == selectedPeriod }
        }
    }
    
    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val total = totalIncome + totalExpense
    
    val incomeSweep = if (total > 0) (totalIncome / total) * 360f else 0f
    val expenseSweep = if (total > 0) (totalExpense / total) * 360f else 0f

    val incomeColor = FintechIncome
    val expenseColor = FintechError
    
    val expenseByCategory = filteredTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second }

    Scaffold(
        containerColor = FintechBackground,
        topBar = { 
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FintechOnSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline)
                ) {
                    Text(selectedPeriod)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Period")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(FintechSurface)
                ) {
                    availableMonths.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period, color = FintechOnSurface) },
                            onClick = {
                                selectedPeriod = period
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Income vs Expense", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FintechOnSurface)
            Spacer(modifier = Modifier.height(32.dp))
            
            if (total == 0.0) {
                Text("Not enough data to display statistics.", color = FintechOutline, fontSize = 16.sp)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(240.dp)) {
                        drawArc(
                            color = incomeColor,
                            startAngle = -90f,
                            sweepAngle = incomeSweep.toFloat(),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 40.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            size = Size(size.width, size.height)
                        )
                        drawArc(
                            color = expenseColor,
                            startAngle = -90f + incomeSweep.toFloat(),
                            sweepAngle = expenseSweep.toFloat(),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 40.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            size = Size(size.width, size.height)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(16.dp), color = incomeColor, shape = RoundedCornerShape(4.dp)) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Income", color = FintechOnSurfaceVariant, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(16.dp), color = expenseColor, shape = RoundedCornerShape(4.dp)) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Expense", color = FintechOnSurfaceVariant, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FintechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Income", fontWeight = FontWeight.SemiBold, color = FintechOnSurfaceVariant, fontSize = 16.sp)
                            Text(viewModel.formatAmount(totalIncome), color = incomeColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Expense", fontWeight = FontWeight.SemiBold, color = FintechOnSurfaceVariant, fontSize = 16.sp)
                            Text(viewModel.formatAmount(totalExpense), color = expenseColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = FintechOutline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Balance", fontWeight = FontWeight.SemiBold, color = FintechOnSurface, fontSize = 18.sp)
                            val netColor = if (totalIncome >= totalExpense) incomeColor else expenseColor
                            Text(viewModel.formatAmount(totalIncome - totalExpense), color = netColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                
                if (expenseByCategory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text("Expense Breakdown", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FintechOnSurface, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = FintechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FintechOutline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            expenseByCategory.forEachIndexed { index, (category, amount) ->
                                val progress = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(category, fontSize = 14.sp, color = FintechOnSurfaceVariant, fontWeight = FontWeight.Medium)
                                        Text(viewModel.formatAmount(amount), fontSize = 14.sp, color = FintechOnSurface, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = FintechSecondary,
                                        trackColor = FintechSecondary.copy(alpha = 0.2f)
                                    )
                                }
                                if (index < expenseByCategory.size - 1) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}
