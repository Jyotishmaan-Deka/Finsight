package com.example.finsight.presentation.screens.goals

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
import com.example.finsight.domain.model.Goal
import com.example.finsight.domain.model.GoalType
import com.example.finsight.presentation.components.EmptyState
import com.example.finsight.presentation.theme.IncomeGreen
import com.example.finsight.presentation.theme.Primary
import com.example.finsight.utils.DateUtils
import com.example.finsight.utils.toCurrency

@Composable
fun GoalsScreen(
    onNavigateToAddGoal: () -> Unit,
    onNavigateToEditGoal: (Long) -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Goals",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.activeGoals.any { it.type == GoalType.SAVINGS }) {
                            IconButton(
                                onClick = {
                                    state.activeGoals
                                        .filter { it.type == GoalType.SAVINGS }
                                        .forEach { viewModel.autoSyncSavingsGoal(it) }
                                },
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Primary.copy(0.1f))
                            ) {
                                Icon(Icons.Filled.Sync, null, tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (state.allGoals.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MiniStatCard("Active", state.activeGoals.size.toString(), "🎯", Modifier.weight(1f))
                        MiniStatCard("Done", state.completedGoals.size.toString(), "✅", Modifier.weight(1f))
                        val maxStreak = state.activeGoals.maxOfOrNull { it.streakDays } ?: 0
                        MiniStatCard("Best Streak", "${maxStreak}d", "🔥", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    listOf("Active (${state.activeGoals.size})", "Completed (${state.completedGoals.size})").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            val displayedGoals = if (selectedTab == 0) state.activeGoals else state.completedGoals

            if (displayedGoals.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = if (selectedTab == 0) "🎯" else "🏆",
                        title = if (selectedTab == 0) "No active goals" else "No completed goals",
                        subtitle = if (selectedTab == 0) "Create a savings goal, no-spend challenge, or budget limit" else "Complete a goal to see it here",
                        action = if (selectedTab == 0) {{
                            Button(onClick = onNavigateToAddGoal) { Text("Create Goal") }
                        }} else null
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onEdit = { onNavigateToEditGoal(goal.id) },
                            onDelete = { viewModel.deleteGoal(goal) },
                            onCheckin = { viewModel.checkinStreak(goal) },
                            onAddProgress = { viewModel.addProgress(goal, it) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddGoal,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 90.dp),
            containerColor = Primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, "Add Goal")
        }
    }
}

@Composable
fun MiniStatCard(label: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCheckin: () -> Unit,
    onAddProgress: (Double) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var progressInput by remember { mutableStateOf("") }

    val goalColor = Color(goal.color)
    val isNoSpend = goal.type == GoalType.NO_SPEND

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(goalColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (goal.type) {
                                GoalType.SAVINGS -> "💰"
                                GoalType.NO_SPEND -> "🔥"
                                GoalType.BUDGET_LIMIT -> "⚡"
                                GoalType.DEBT_PAYOFF -> "📉"
                            },
                            fontSize = 22.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = goal.type.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = goalColor
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (isNoSpend) {
                // Streak display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${goal.streakDays}",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = goalColor
                        )
                        Text(
                            text = "days streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (goal.lastCheckinDate != null) {
                            Text(
                                text = "Last check-in",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = DateUtils.smartDateLabel(goal.lastCheckinDate),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onCheckin,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = goalColor),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Check In Today", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(7) { i ->
                        val isActive = i < goal.streakDays.coerceAtMost(7)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isActive) goalColor else goalColor.copy(alpha = 0.2f))
                        )
                    }
                }
            } else {
                // Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = goal.currentAmount.toCurrency(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = goalColor
                        )
                        Text("saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = goal.targetAmount.toCurrency(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { goal.progressPercent },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = goalColor,
                    trackColor = goalColor.copy(alpha = 0.15f)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(goal.progressPercent * 100).toInt()}% complete • ${goal.remainingAmount.toCurrency()} to go",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!goal.isCompleted) {
                        TextButton(
                            onClick = { showProgressDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp), tint = goalColor)
                            Spacer(Modifier.width(2.dp))
                            Text("Add", style = MaterialTheme.typography.labelMedium, color = goalColor)
                        }
                    } else {
                        Text("🎉 Completed!", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = IncomeGreen)
                    }
                }

                if (goal.deadline != null) {
                    Text(
                        text = if (goal.isOverdue) "⚠️ Overdue" else "🗓️ Due ${DateUtils.formatDate(goal.deadline)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (goal.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete \"${goal.title}\"?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showProgressDialog) {
        AlertDialog(
            onDismissRequest = { showProgressDialog = false },
            title = { Text("Add Progress") },
            text = {
                OutlinedTextField(
                    value = progressInput,
                    onValueChange = { progressInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    progressInput.toDoubleOrNull()?.let { onAddProgress(it) }
                    showProgressDialog = false
                    progressInput = ""
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showProgressDialog = false }) { Text("Cancel") }
            }
        )
    }
}
