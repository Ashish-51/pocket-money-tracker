package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionType
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MainViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val selectedType by viewModel.filterType.collectAsState()

    Scaffold(
        containerColor = FintechBackground,
        topBar = { 
            TopAppBar(
                title = { Text("Search & Filters", fontWeight = FontWeight.Bold, color = FintechOnSurface) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FintechBackground)
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search by note or category...") },
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
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FintechPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = FintechPrimary,
                        containerColor = FintechSurface,
                        labelColor = FintechOnSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = FintechOutline.copy(alpha = 0.2f),
                        selectedBorderColor = FintechPrimary,
                        enabled = true,
                        selected = selectedType == null
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { viewModel.setFilterType(TransactionType.INCOME) },
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
                        selected = selectedType == TransactionType.INCOME
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { viewModel.setFilterType(TransactionType.EXPENSE) },
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
                        selected = selectedType == TransactionType.EXPENSE
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No transactions match criteria.", color = FintechOutline, fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(transactions) { tx ->
                        TransactionItem(tx, formatter = { viewModel.formatAmount(it) }, onDelete = { viewModel.deleteTransaction(tx.transactionId) })
                    }
                    item { Spacer(modifier = Modifier.height(84.dp)) }
                }
            }
        }
    }
}
