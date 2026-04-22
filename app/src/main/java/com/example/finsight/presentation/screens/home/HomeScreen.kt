package com.example.finsight.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.finsight.domain.model.Goal
import com.example.finsight.domain.model.GoalType
import com.example.finsight.domain.model.Transaction
import com.example.finsight.presentation.components.*
import com.example.finsight.presentation.theme.*
import com.example.finsight.utils.toCurrency
import com.example.finsight.utils.toSmartDateLabel
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val greeting = getGreeting()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 0.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Your Finance",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Balance Card
                    BalanceCard(
                        balance = state.totalBalance,
                        income = state.thisMonthIncome,
                        expense = state.thisMonthExpense,
                        onAddTransaction = onNavigateToAddTransaction
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Income",
                    amount = state.totalIncome.toCurrency(),
                    icon = Icons.Filled.ArrowDownward,
                    iconColor = IncomeGreen,
                    backgroundColor = IncomeGreen.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Expenses",
                    amount = state.totalExpenses.toCurrency(),
                    icon = Icons.Filled.ArrowUpward,
                    iconColor = ExpenseRed,
                    backgroundColor = ExpenseRed.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Breakdown
            if (state.categoryBreakdown.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(
                        title = "This Month's Spending",
                        action = {
                            TextButton(onClick = onNavigateToTransactions) {
                                Text("See All", style = MaterialTheme.typography.labelMedium, color = Primary)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DonutChart(
                                entries = state.categoryBreakdown.take(6).mapIndexed { idx, (cat, amount) ->
                                    ChartEntry(
                                        label = cat.displayName,
                                        value = amount.toFloat(),
                                        color = Color(cat.colorHex)
                                    )
                                },
                                modifier = Modifier.size(110.dp),
                                centerLabel = state.thisMonthExpense.let {
                                    if (it > 0) "₹${String.format("%.0f", it / 1000)}K" else "₹0"
                                },
                                centerSubLabel = "spent"
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.categoryBreakdown.take(4).forEach { (cat, amount) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(cat.colorHex))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cat.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = amount.toCurrency(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Active Goals Preview
            if (state.activeGoals.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(title = "Active Goals")
                    Spacer(modifier = Modifier.height(12.dp))
                    state.activeGoals.forEach { goal ->
                        GoalPreviewCard(goal = goal)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Transactions
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionHeader(
                    title = "Recent Transactions",
                    action = {
                        TextButton(onClick = onNavigateToTransactions) {
                            Text("See All", style = MaterialTheme.typography.labelMedium, color = Primary)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (state.recentTransactions.isEmpty() && !state.isLoading) {
                    EmptyState(
                        emoji = "💳",
                        title = "No transactions yet",
                        subtitle = "Tap the + button to add your first transaction",
                        action = {
                            Button(onClick = onNavigateToAddTransaction) {
                                Text("Add Transaction")
                            }
                        }
                    )
                } else {
                    state.recentTransactions.forEachIndexed { idx, tx ->
                        TransactionItem(
                            transaction = tx,
                            onClick = { onNavigateToEditTransaction(tx.id) },
                            onDelete = { viewModel.deleteTransaction(tx) },
                            animationDelay = idx * 60
                        )
                        if (idx < state.recentTransactions.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
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

@Composable
fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    onAddTransaction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF6C63FF), Color(0xFF4ECDC4))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = balance.toCurrency(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BalanceStat(
                    label = "This Month In",
                    amount = income,
                    icon = Icons.Filled.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
                BalanceStat(
                    label = "This Month Out",
                    amount = expense,
                    icon = Icons.Filled.ArrowUpward,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BalanceStat(label: String, amount: Double, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            Text(
                text = amount.toCurrency(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
fun GoalPreviewCard(goal: Goal) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (goal.type == GoalType.NO_SPEND) "🔥" else "🎯",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (goal.type == GoalType.NO_SPEND) {
                            Text(
                                text = "${goal.streakDays} day streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (goal.type != GoalType.NO_SPEND) {
                    Text(
                        text = "${(goal.progressPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(goal.color)
                    )
                }
            }
            if (goal.type != GoalType.NO_SPEND) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { goal.progressPercent },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(goal.color),
                    trackColor = Color(goal.color).copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = goal.currentAmount.toCurrency(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = goal.targetAmount.toCurrency(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning ☀️"
        in 12..16 -> "Good afternoon 🌤️"
        in 17..20 -> "Good evening 🌇"
        else -> "Good night 🌙"
    }
}
