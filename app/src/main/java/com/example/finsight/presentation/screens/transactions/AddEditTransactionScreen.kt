package com.example.finsight.presentation.screens.transactions

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.finsight.data.repository.TransactionRepository
import com.example.finsight.domain.model.Category
import com.example.finsight.domain.model.Transaction
import com.example.finsight.domain.model.TransactionType
import com.example.finsight.presentation.theme.ExpenseRed
import com.example.finsight.presentation.theme.IncomeGreen
import com.example.finsight.presentation.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ViewModel
@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    var existingTransaction: Transaction? = null
        private set

    suspend fun loadTransaction(id: Long): Transaction? {
        return repository.getTransactionById(id)?.also { existingTransaction = it }
    }

    fun saveTransaction(
        amount: Double,
        type: TransactionType,
        category: Category,
        description: String,
        date: Long,
        note: String,
        existingId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val transaction = Transaction(
                id = existingId ?: 0L,
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = date,
                note = note
            )
            if (existingId != null && existingId > 0) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }
            _isLoading.value = false
            _isSaved.value = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel()
) {
    val isSaved by viewModel.isSaved.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(Category.FOOD) }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    // Load existing transaction if editing
    LaunchedEffect(transactionId) {
        if (transactionId != null && transactionId > 0) {
            viewModel.loadTransaction(transactionId)?.let { tx ->
                amount = tx.amount.toString()
                selectedType = tx.type
                selectedCategory = tx.category
                description = tx.description
                note = tx.note
                selectedDate = tx.date
                isEditMode = true
            }
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) onNavigateBack()
    }

    // Update category options when type changes
    LaunchedEffect(selectedType) {
        selectedCategory = if (selectedType == TransactionType.INCOME)
            Category.SALARY else Category.FOOD
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 52.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isEditMode) "Edit Transaction" else "New Transaction",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TransactionType.entries.forEach { type ->
                val isSelected = selectedType == type
                val color = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedType = type }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (type == TransactionType.INCOME) "↓" else "↑",
                            fontSize = 18.sp,
                            color = color
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount field
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Amount",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it.filter { c -> c.isDigit() || c == '.' }
                    amountError = false
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.titleMedium, color = Primary, modifier = Modifier.padding(start = 4.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {{ Text("Please enter a valid amount") }} else null,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category picker
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Category",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            val categories = if (selectedType == TransactionType.INCOME)
                Category.incomeCategories() else Category.expenseCategories()

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.heightIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(cat.colorHex).copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(cat.colorHex) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(cat.emoji, fontSize = 22.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = cat.displayName.split(" ").first(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(cat.colorHex) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Description",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What was this for?") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Date",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = dateFormat.format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                trailingIcon = {
                    Icon(Icons.Filled.CalendarMonth, null, tint = Primary)
                },
                enabled = false,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Note (optional)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note...") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save button
        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                if (parsedAmount == null || parsedAmount <= 0) {
                    amountError = true
                    return@Button
                }
                viewModel.saveTransaction(
                    amount = parsedAmount,
                    type = selectedType,
                    category = selectedCategory,
                    description = description,
                    date = selectedDate,
                    note = note,
                    existingId = if (isEditMode) transactionId else null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Check, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditMode) "Update Transaction" else "Save Transaction",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
