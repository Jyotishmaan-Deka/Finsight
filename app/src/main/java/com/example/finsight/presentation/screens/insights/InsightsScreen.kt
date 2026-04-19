package com.example.finsight.presentation.screens.insights

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finsight.presentation.components.*
import com.example.finsight.presentation.theme.*
import com.example.finsight.utils.toCurrency

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Use conditional rendering instead of early returns
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            state.totalTransactions == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        emoji = "📊",
                        title = "No data yet",
                        subtitle = "Add some transactions to see your insights"
                    )
                }
            }
            else -> {
                // Content when data is available
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 52.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Your spending patterns & trends",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Key metrics row
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InsightMetricCard(
                            label = "Savings Rate",
                            value = "${state.savingsRate.toInt()}%",
                            emoji = "💹",
                            color = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        InsightMetricCard(
                            label = "Avg Daily Spend",
                            value = state.avgDailySpend.toCurrency(),
                            emoji = "📅",
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        InsightMetricCard(
                            label = "Transactions",
                            value = state.totalTransactions.toString(),
                            emoji = "💳",
                            color = WarningOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Weekly comparison
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader(title = "Week vs Last Week")
                            Spacer(Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                WeekComparisonBar(
                                    thisWeek = state.thisWeekExpense.toFloat(),
                                    lastWeek = state.lastWeekExpense.toFloat(),
                                    modifier = Modifier.width(80.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                                    WeekStatRow("This week", state.thisWeekExpense, Primary)
                                    Spacer(Modifier.height(8.dp))
                                    WeekStatRow("Last week", state.lastWeekExpense, MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(10.dp))

                                    val diff = state.thisWeekExpense - state.lastWeekExpense
                                    val pct = if (state.lastWeekExpense > 0) (diff / state.lastWeekExpense * 100).toInt() else 0
                                    val isUp = diff > 0
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background((if (isUp) ExpenseRed else IncomeGreen).copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            if (isUp) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                            null,
                                            tint = if (isUp) ExpenseRed else IncomeGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "${if (isUp) "+" else ""}${pct}% vs last week",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isUp) ExpenseRed else IncomeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily spend - last 7 days line chart
                    if (state.dailyLast7.any { it.amount > 0 }) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SectionHeader(title = "Daily Spending (Last 7 Days)")
                                Spacer(Modifier.height(16.dp))

                                LineChart(
                                    dataPoints = state.dailyLast7.map { it.amount.toFloat() },
                                    labels = state.dailyLast7.map { it.day },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    lineColor = Primary,
                                    fillColor = Primary
                                )

                                Spacer(Modifier.height(8.dp))

                                // Day labels
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    state.dailyLast7.forEach { d ->
                                        Text(
                                            text = d.day,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Category breakdown
                    if (state.topCategories.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SectionHeader(title = "Spending by Category")
                                Text(
                                    text = "This month",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DonutChart(
                                        entries = state.topCategories.take(6).map { cs ->
                                            ChartEntry(cs.category.displayName, cs.amount.toFloat(), Color(cs.category.colorHex))
                                        },
                                        modifier = Modifier.size(120.dp),
                                        centerLabel = state.thisMonthExpense.let { "₹${String.format("%.0f", it/1000)}K" },
                                        centerSubLabel = "total"
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        state.topCategories.take(5).forEach { cs ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(cs.category.emoji, fontSize = 14.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        cs.category.displayName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    LinearProgressIndicator(
                                                        progress = { cs.percentage / 100f },
                                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                        color = Color(cs.category.colorHex),
                                                        trackColor = Color(cs.category.colorHex).copy(0.15f)
                                                    )
                                                }
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "${cs.percentage.toInt()}%",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(cs.category.colorHex)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Bar chart
                                HorizontalBarChart(
                                    entries = state.topCategories.take(6).map { cs ->
                                        ChartEntry(cs.category.displayName.split(" ").first(), cs.amount.toFloat(), Color(cs.category.colorHex))
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 6-month trend
                    if (state.monthly6Data.any { it.income > 0 || it.expense > 0 }) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SectionHeader(title = "6-Month Trend")
                                Spacer(Modifier.height(16.dp))

                                // Income vs expense dual bar
                                state.monthly6Data.forEach { md ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = md.month,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(36.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val maxVal = state.monthly6Data.maxOf { maxOf(it.income, it.expense) }.let { if (it == 0.0) 1.0 else it }

                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Box(modifier = Modifier.fillMaxWidth().height(7.dp)) {
                                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                                    val incomeW = size.width * (md.income / maxVal).toFloat()
                                                    drawRoundRect(color = IncomeGreen.copy(0.2f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                                                    if (incomeW > 0) drawRoundRect(color = IncomeGreen, size = size.copy(width = incomeW), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                                                }
                                            }
                                            Box(modifier = Modifier.fillMaxWidth().height(7.dp)) {
                                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                                    val expW = size.width * (md.expense / maxVal).toFloat()
                                                    drawRoundRect(color = ExpenseRed.copy(0.2f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                                                    if (expW > 0) drawRoundRect(color = ExpenseRed, size = size.copy(width = expW), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(10.dp).clip(CircleShape).background(IncomeGreen))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(10.dp).clip(CircleShape).background(ExpenseRed))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun InsightMetricCard(
    label: String,
    value: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeekStatRow(label: String, amount: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(amount.toCurrency(), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}