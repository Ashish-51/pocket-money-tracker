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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(viewModel: MainViewModel) {
    val budgets by viewModel.budgets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FintechBackground,
        topBar = { 
            TopAppBar(
                title = { Text("Budgets", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FintechBackground)
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FintechPrimary,
                contentColor = FintechOnPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 8.dp).size(64.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Budget", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        if (budgets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No budgets set yet.", color = FintechOutline, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(budgets) { budget ->
                    BudgetItem(
                        budget = budget,
                        formatter = { viewModel.formatAmountNoDecimals(it) },
                        onDelete = { viewModel.deleteBudget(budget.budgetId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(84.dp)) }
            }
        }

        if (showAddDialog) {
            AddBudgetDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { category, limit ->
                    viewModel.addBudget(category, limit)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BudgetItem(budget: Budget, formatter: (Double) -> String, onDelete: () -> Unit) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(FintechSecondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("★", color = FintechSecondary, fontSize = 24.sp)
                }
                Column {
                    Text(text = budget.category, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = FintechOnSurface)
                    Text(text = "Limit: ${formatter(budget.amountLimit)}", fontSize = 14.sp, color = FintechOnSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = FintechError.copy(alpha = 0.8f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onAdd: (String, Double) -> Unit) {
    var category by remember { mutableStateOf("") }
    var limitStr by remember { mutableStateOf("") }
    
    val expenseCategories = listOf("Food", "Travel", "Shopping", "Entertainment", "Bills", "Other")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FintechSurface,
        titleContentColor = FintechOnSurface,
        textContentColor = FintechOnSurfaceVariant,
        title = { Text("Add Monthly Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedTextColor = FintechOnSurface,
                            unfocusedTextColor = FintechOnSurfaceVariant,
                            focusedBorderColor = FintechPrimary,
                            unfocusedBorderColor = FintechOutline,
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(FintechSurface)
                    ) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = FintechOnSurface) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Budget Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FintechOnSurface,
                        unfocusedTextColor = FintechOnSurfaceVariant,
                        focusedBorderColor = FintechPrimary,
                        unfocusedBorderColor = FintechOutline,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitStr.toDoubleOrNull() ?: 0.0
                    if (category.isNotBlank() && limit > 0) {
                        onAdd(category, limit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FintechPrimary, contentColor = FintechOnPrimary),
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text("Add", fontWeight = FontWeight.Bold) 
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = FintechOnSurfaceVariant)
            ) { 
                Text("Cancel") 
            }
        }
    )
}
