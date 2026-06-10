package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionType
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    val incomeCategories = listOf("Pocket Money", "Allowance", "Salary", "Gift", "Other")
    val expenseCategories = listOf("Food", "Travel", "Shopping", "Entertainment", "Bills", "Other")
    val paymentMethods = listOf("Cash", "Card", "UPI", "Bank Transfer")

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedPayment by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FintechBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FintechBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME; category = incomeCategories.first() },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FintechIncome.copy(alpha = 0.1f),
                        selectedLabelColor = FintechIncome,
                        containerColor = FintechSurface,
                        labelColor = FintechOnSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = FintechOutline.copy(alpha = 0.2f),
                        selectedBorderColor = FintechIncome,
                        enabled = true,
                        selected = type == TransactionType.INCOME
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                )
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE; category = expenseCategories.first() },
                    label = { Text("Expense") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FintechError.copy(alpha = 0.1f),
                        selectedLabelColor = FintechError,
                        containerColor = FintechSurface,
                        labelColor = FintechOnSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = FintechOutline.copy(alpha = 0.2f),
                        selectedBorderColor = FintechError,
                        enabled = true,
                        selected = type == TransactionType.EXPENSE
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = FintechOnSurface,
                    unfocusedTextColor = FintechOnSurfaceVariant,
                    focusedBorderColor = FintechPrimary,
                    unfocusedBorderColor = FintechOutline.copy(alpha = 0.3f),
                    focusedLabelColor = FintechPrimary,
                    cursorColor = FintechPrimary
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val currentCategories = if (type == TransactionType.INCOME) incomeCategories else expenseCategories
            if (category.isEmpty() || !currentCategories.contains(category)) {
                category = currentCategories.first()
            }

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FintechOnSurface,
                        unfocusedTextColor = FintechOnSurfaceVariant,
                        focusedBorderColor = FintechPrimary,
                        unfocusedBorderColor = FintechOutline.copy(alpha = 0.3f),
                        focusedLabelColor = FintechPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false },
                    modifier = Modifier.background(FintechSurface)
                ) {
                    currentCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = FintechOnSurface) },
                            onClick = {
                                category = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            ExposedDropdownMenuBox(
                expanded = expandedPayment,
                onExpandedChange = { expandedPayment = it }
            ) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FintechOnSurface,
                        unfocusedTextColor = FintechOnSurfaceVariant,
                        focusedBorderColor = FintechPrimary,
                        unfocusedBorderColor = FintechOutline.copy(alpha = 0.3f),
                        focusedLabelColor = FintechPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedPayment,
                    onDismissRequest = { expandedPayment = false },
                    modifier = Modifier.background(FintechSurface)
                ) {
                    paymentMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method, color = FintechOnSurface) },
                            onClick = {
                                paymentMethod = method
                                expandedPayment = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = FintechOnSurface,
                    unfocusedTextColor = FintechOnSurfaceVariant,
                    focusedBorderColor = FintechPrimary,
                    unfocusedBorderColor = FintechOutline.copy(alpha = 0.3f),
                    focusedLabelColor = FintechPrimary,
                    cursorColor = FintechPrimary
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        viewModel.addTransaction(amt, type, category, note, paymentMethod, Date())
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FintechPrimary, contentColor = FintechOnPrimary)
            ) {
                Text("Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
