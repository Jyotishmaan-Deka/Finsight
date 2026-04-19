package com.example.finsight.presentation.screens.goals

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finsight.data.repository.GoalRepository
import com.example.finsight.domain.model.Goal
import com.example.finsight.domain.model.GoalType
import com.example.finsight.presentation.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditGoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    suspend fun loadGoal(id: Long): Goal? = goalRepository.getGoalById(id)

    fun saveGoal(
        title: String,
        targetAmount: Double,
        type: GoalType,
        deadline: Long?,
        color: Long,
        existingId: Long? = null,
        currentAmount: Double = 0.0,
        streakDays: Int = 0,
        lastCheckinDate: Long? = null
    ) {
        viewModelScope.launch {
            val goal = Goal(
                id = existingId ?: 0L,
                title = title,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                type = type,
                deadline = deadline,
                color = color,
                streakDays = streakDays,
                lastCheckinDate = lastCheckinDate
            )
            if (existingId != null && existingId > 0) goalRepository.updateGoal(goal)
            else goalRepository.insertGoal(goal)
            _isSaved.value = true
        }
    }
}

val goalColorOptions = listOf(
    0xFF6C63FF, 0xFF00C853, 0xFFFF4757, 0xFFFF9F43,
    0xFF00BCD4, 0xFFAB47BC, 0xFF29B6F6, 0xFFFF6584
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalScreen(
    goalId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditGoalViewModel = hiltViewModel()
) {
    val isSaved by viewModel.isSaved.collectAsState()

    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GoalType.SAVINGS) }
    var selectedColor by remember { mutableLongStateOf(0xFF6C63FF) }
    var deadline by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var existingCurrentAmount by remember { mutableDoubleStateOf(0.0) }
    var existingStreakDays by remember { mutableIntStateOf(0) }
    var existingLastCheckin by remember { mutableStateOf<Long?>(null) }
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) {
        if (goalId != null && goalId > 0) {
            viewModel.loadGoal(goalId)?.let { g ->
                title = g.title
                targetAmount = g.targetAmount.toString()
                selectedType = g.type
                selectedColor = g.color
                deadline = g.deadline
                isEditMode = true
                existingCurrentAmount = g.currentAmount
                existingStreakDays = g.streakDays
                existingLastCheckin = g.lastCheckinDate
            }
        }
    }

    LaunchedEffect(isSaved) { if (isSaved) onNavigateBack() }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = deadline ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { deadline = state.selectedDateMillis; showDatePicker = false }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 52.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Filled.ArrowBack, null)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (isEditMode) "Edit Goal" else "New Goal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(12.dp))

        // Goal Type Selection
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Goal Type", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            GoalType.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { type ->
                        val isSelected = selectedType == type
                        val emoji = when (type) {
                            GoalType.SAVINGS -> "💰"
                            GoalType.NO_SPEND -> "🔥"
                            GoalType.BUDGET_LIMIT -> "⚡"
                            GoalType.DEBT_PAYOFF -> "📉"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(selectedColor).copy(0.15f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(selectedColor) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) Color(selectedColor) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Title
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Goal Title", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                placeholder = { Text("e.g. Emergency Fund, No-Spend November") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Target Amount (hide for no-spend)
        if (selectedType != GoalType.NO_SPEND) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = when (selectedType) {
                        GoalType.BUDGET_LIMIT -> "Budget Limit"
                        GoalType.DEBT_PAYOFF -> "Total Debt"
                        else -> "Target Amount"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it.filter { c -> c.isDigit() || c == '.' }; amountError = false },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium, color = Primary, modifier = Modifier.padding(start = 4.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Deadline
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Deadline (optional)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (deadline != null) dateFormat.format(Date(deadline!!)) else "No deadline",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = Primary) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant, disabledTextColor = MaterialTheme.colorScheme.onSurface)
                )
                if (deadline != null) {
                    IconButton(onClick = { deadline = null }, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Icon(Icons.Filled.Close, null)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Color picker
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Color", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                goalColorOptions.forEach { colorHex ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex))
                            .then(if (selectedColor == colorHex) Modifier.border(3.dp, MaterialTheme.colorScheme.background, CircleShape).border(5.dp, Color(colorHex), CircleShape) else Modifier)
                            .clickable { selectedColor = colorHex }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Save
        Button(
            onClick = {
                var hasError = false
                if (title.isBlank()) { titleError = true; hasError = true }
                if (selectedType != GoalType.NO_SPEND && (targetAmount.toDoubleOrNull() ?: 0.0) <= 0) {
                    amountError = true; hasError = true
                }
                if (!hasError) {
                    viewModel.saveGoal(
                        title = title,
                        targetAmount = if (selectedType == GoalType.NO_SPEND) 0.0 else targetAmount.toDoubleOrNull() ?: 0.0,
                        type = selectedType,
                        deadline = deadline,
                        color = selectedColor,
                        existingId = if (isEditMode) goalId else null,
                        currentAmount = existingCurrentAmount,
                        streakDays = existingStreakDays,
                        lastCheckinDate = existingLastCheckin
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(selectedColor))
        ) {
            Icon(Icons.Filled.Check, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(if (isEditMode) "Update Goal" else "Create Goal", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
        }

        Spacer(Modifier.height(40.dp))
    }
}
