package com.example.finsight.presentation.screens.transactions

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finsight.domain.model.Transaction
import com.example.finsight.presentation.components.EmptyState
import com.example.finsight.presentation.components.TransactionItem
import com.example.finsight.presentation.theme.ExpenseRed
import com.example.finsight.presentation.theme.IncomeGreen
import com.example.finsight.presentation.theme.Primary
import com.example.finsight.utils.DateUtils
import com.example.finsight.utils.toCurrency

@Composable
fun TransactionsScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Search bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Search transactions...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Close, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionFilter.entries.forEach { filter ->
                        val selected = state.filter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.15f),
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }
            }

            // Summary strip
            if (state.filteredTransactions.isNotEmpty()) {
                val totalIncome = state.filteredTransactions
                    .filter { it.type.name == "INCOME" }
                    .sumOf { it.amount }
                val totalExpense = state.filteredTransactions
                    .filter { it.type.name == "EXPENSE" }
                    .sumOf { it.amount }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("↓", color = IncomeGreen, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("Income", style = MaterialTheme.typography.labelSmall, color = IncomeGreen.copy(0.7f))
                                Text(totalIncome.toCurrency(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = IncomeGreen)
                            }
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("↑", color = ExpenseRed, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("Expenses", style = MaterialTheme.typography.labelSmall, color = ExpenseRed.copy(0.7f))
                                Text(totalExpense.toCurrency(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Transactions list
            if (state.filteredTransactions.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = if (state.searchQuery.isNotBlank()) "🔍" else "📭",
                        title = if (state.searchQuery.isNotBlank()) "No results found" else "No transactions yet",
                        subtitle = if (state.searchQuery.isNotBlank()) "Try a different search term" else "Tap + to add your first transaction"
                    )
                }
            } else {
                // Group by date
                val grouped = state.filteredTransactions.groupBy { tx ->
                    DateUtils.getStartOfDay(tx.date)
                }.toSortedMap(compareByDescending { it })

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (dayStart, transactions) ->
                        item(key = "header_$dayStart") {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateUtils.smartDateLabel(dayStart),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = transactions.sumOf { if (it.type.name == "INCOME") it.amount else -it.amount }.let {
                                        (if (it >= 0) "+" else "") + it.toCurrency()
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (transactions.sumOf { if (it.type.name == "INCOME") it.amount else -it.amount } >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }
                        itemsIndexed(
                            items = transactions,
                            key = { _, tx -> tx.id }
                        ) { idx, tx ->
                            TransactionItem(
                                transaction = tx,
                                onClick = { onNavigateToEditTransaction(tx.id) },
                                onDelete = { viewModel.deleteTransaction(tx) },
                                animationDelay = idx * 40
                            )
                        }
                        item { Spacer(Modifier.height(4.dp)) }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp),
            containerColor = Primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
        }
    }
}
