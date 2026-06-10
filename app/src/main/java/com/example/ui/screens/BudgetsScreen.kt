package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Budget
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import com.example.data.RecurringTransaction
import com.example.data.TransactionType
import java.util.Date
import java.util.Calendar

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(viewModel: MainViewModel, onOpenDrawer: () -> Unit) {
    val recurring by viewModel.recurringTransactions.collectAsState()
    var showAddRecurring by remember { mutableStateOf(false) }
    var editingRtx by remember { mutableStateOf<RecurringTransaction?>(null) }

    Scaffold(
        containerColor = FintechBackground,
        topBar = { 
            TopAppBar(
                title = { Text("Subscriptions", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRecurring = true },
                containerColor = FintechPrimary,
                contentColor = FintechOnPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 8.dp).size(64.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (recurring.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No recurring transactions.", color = FintechOutline, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    items(recurring, key = { it.recurringId }) { rtx ->
                        RecurringItem(
                            rtx = rtx,
                            formatter = { viewModel.formatAmount(it) },
                            onEdit = { editingRtx = rtx },
                            onDelete = { viewModel.deleteRecurringTransaction(rtx.recurringId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        if (showAddRecurring) {
            AddRecurringDialog(
                onDismiss = { showAddRecurring = false },
                onAdd = { amt, type, cat, note, pm, interval, nextDate ->
                    viewModel.addRecurringTransaction(amt, type, cat, note, pm, interval, nextDate)
                    showAddRecurring = false
                }
            )
        }
        
        editingRtx?.let { rtx ->
            EditRecurringDialog(
                rtx = rtx,
                onDismiss = { editingRtx = null },
                onSave = { updatedRtx ->
                    viewModel.updateRecurringTransaction(updatedRtx)
                    editingRtx = null
                }
            )
        }
    }
}

@Composable
fun RecurringItem(rtx: com.example.data.RecurringTransaction, formatter: (Double) -> String, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(FintechSecondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔁", color = FintechSecondary, fontSize = 24.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    val mainText = if (rtx.note.isNotBlank()) rtx.note else rtx.category
                    Text(text = mainText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = FintechOnSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    val nextDateStr = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(rtx.nextProcessingDate.toDate())
                    val subText = buildString {
                        if (rtx.note.isNotBlank()) append("${rtx.category} • ")
                        append("${rtx.interval} • ${formatter(rtx.amount)} • Next: $nextDateStr")
                    }
                    Text(text = subText, fontSize = 13.sp, color = FintechOnSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = FintechPrimary.copy(alpha = 0.8f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = FintechError.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringDialog(onDismiss: () -> Unit, onAdd: (Double, com.example.data.TransactionType, String, String, String, String, java.util.Date) -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val type = com.example.data.TransactionType.EXPENSE
    val category = "Subscription"
    var paymentMethod by remember { mutableStateOf("Cash") }
    var interval by remember { mutableStateOf("Monthly") }
    
    val intervals = listOf("Monthly", "Yearly")
    val paymentMethods = listOf("Cash", "Card", "UPI", "Bank Transfer")

    var expandedInt by remember { mutableStateOf(false) }
    var expandedPay by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FintechSurface,
        titleContentColor = FintechOnSurface,
        textContentColor = FintechOnSurfaceVariant,
        title = { Text("Add Subscription", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Service Name (e.g. Netflix)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedInt,
                    onExpandedChange = { expandedInt = !expandedInt }
                ) {
                    OutlinedTextField(
                        value = interval,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Interval") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInt) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInt,
                        onDismissRequest = { expandedInt = false },
                        modifier = Modifier.background(FintechSurface)
                    ) {
                        intervals.forEach { i ->
                            DropdownMenuItem(text = { Text(i, color = FintechOnSurface) }, onClick = { interval = i; expandedInt = false })
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedPay,
                    onExpandedChange = { expandedPay = !expandedPay }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPay) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPay,
                        onDismissRequest = { expandedPay = false },
                        modifier = Modifier.background(FintechSurface)
                    ) {
                        paymentMethods.forEach { p ->
                            DropdownMenuItem(text = { Text(p, color = FintechOnSurface) }, onClick = { paymentMethod = p; expandedPay = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onAdd(amt, type, category, note, paymentMethod, interval, java.util.Date())
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecurringDialog(rtx: com.example.data.RecurringTransaction, onDismiss: () -> Unit, onSave: (com.example.data.RecurringTransaction) -> Unit) {
    var amountStr by remember { mutableStateOf(if (rtx.amount % 1.0 == 0.0) rtx.amount.toInt().toString() else rtx.amount.toString()) }
    var note by remember { mutableStateOf(rtx.note) }
    val type = com.example.data.TransactionType.EXPENSE
    val category = "Subscription"
    var paymentMethod by remember { mutableStateOf(rtx.paymentMethod) }
    var interval by remember { mutableStateOf(rtx.interval) }
    
    val intervals = listOf("Monthly", "Yearly")
    val paymentMethods = listOf("Cash", "Card", "UPI", "Bank Transfer")

    var expandedInt by remember { mutableStateOf(false) }
    var expandedPay by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FintechSurface,
        titleContentColor = FintechOnSurface,
        textContentColor = FintechOnSurfaceVariant,
        title = { Text("Edit Subscription", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Service Name (e.g. Netflix)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedInt,
                    onExpandedChange = { expandedInt = !expandedInt }
                ) {
                    OutlinedTextField(
                        value = interval,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Interval") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInt) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInt,
                        onDismissRequest = { expandedInt = false },
                        modifier = Modifier.background(FintechSurface)
                    ) {
                        intervals.forEach { i ->
                            DropdownMenuItem(text = { Text(i, color = FintechOnSurface) }, onClick = { interval = i; expandedInt = false })
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedPay,
                    onExpandedChange = { expandedPay = !expandedPay }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPay) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPay,
                        onDismissRequest = { expandedPay = false },
                        modifier = Modifier.background(FintechSurface)
                    ) {
                        paymentMethods.forEach { p ->
                            DropdownMenuItem(text = { Text(p, color = FintechOnSurface) }, onClick = { paymentMethod = p; expandedPay = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        val updated = rtx.copy(
                            amount = amt,
                            note = note,
                            type = type,
                            category = category,
                            paymentMethod = paymentMethod,
                            interval = interval
                        )
                        onSave(updated)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

