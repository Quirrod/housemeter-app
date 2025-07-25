package com.jarrod.house.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.text.SimpleDateFormat
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.data.model.Debt
import com.jarrod.house.ui.viewmodel.ApartmentsViewModel
import com.jarrod.house.ui.viewmodel.DebtViewModel
import com.jarrod.house.ui.components.ConfirmDialog

private fun formatDate(dateString: String): String {
    return try {
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormatter.parse(dateString)
        outputFormatter.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsManagementScreen(
    onBackClick: () -> Unit,
    debtViewModel: DebtViewModel = viewModel(),
    apartmentsViewModel: ApartmentsViewModel = viewModel()
) {
    val context = LocalContext.current
    var showCreateDebtDialog by remember { mutableStateOf(false) }
    var showEditDebtDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedDebt by remember { mutableStateOf<Debt?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }
    
    val debts by debtViewModel.debts.collectAsState()
    val apartments by apartmentsViewModel.apartments.collectAsState()
    val isLoading by debtViewModel.isLoading.collectAsState()
    val error by debtViewModel.error.collectAsState()
    val createDebtResult by debtViewModel.createResult.collectAsState()
    val updateDebtResult by debtViewModel.updateResult.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todas", "Pendientes", "Pagadas", "Vencidas")

    // Load data when screen starts
    LaunchedEffect(Unit) {
        debtViewModel.loadDebts(context)
        apartmentsViewModel.loadApartments(context)
    }

    // Handle create results
    LaunchedEffect(createDebtResult) {
        createDebtResult?.let { result ->
            if (result.isSuccess) {
                showCreateDebtDialog = false
                debtViewModel.clearCreateResult()
            }
        }
    }

    LaunchedEffect(updateDebtResult) {
        updateDebtResult?.let { result ->
            if (result.isSuccess) {
                showEditDebtDialog = false
                selectedDebt = null
                debtViewModel.clearCreateResult()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Gestión de Deudas") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                IconButton(onClick = { showCreateDebtDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Crear deuda")
                }
            }
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val filteredDebts = when (selectedTab) {
                0 -> debts
                1 -> debts.filter { it.status == "pending" }
                2 -> debts.filter { it.status == "paid" }
                3 -> debts.filter { it.status == "overdue" }
                else -> debts
            }

            if (filteredDebts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            when (selectedTab) {
                                1 -> Icons.Default.PendingActions
                                2 -> Icons.Default.CheckCircle
                                3 -> Icons.Default.Warning
                                else -> Icons.Default.Receipt
                            },
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedTab) {
                                1 -> "No hay deudas pendientes"
                                2 -> "No hay deudas pagadas"
                                3 -> "No hay deudas vencidas"
                                else -> "No hay deudas registradas"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredDebts) { debt ->
                        DebtCard(
                            debt = debt,
                            onEdit = { 
                                selectedDebt = debt
                                showEditDebtDialog = true
                            },
                            onDelete = { 
                                debtToDelete = debt
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Error handling
        error?.let { errorMsg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(errorMsg, modifier = Modifier.weight(1f))
                    TextButton(onClick = { debtViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            }
        }
    }

    if (showCreateDebtDialog) {
        CreateDebtDialog(
            apartments = apartments,
            onDismiss = { showCreateDebtDialog = false },
            onConfirm = { apartmentId, amount, description, dueDate ->
                debtViewModel.createDebt(context, apartmentId, amount, description, dueDate)
            }
        )
    }

    if (showEditDebtDialog && selectedDebt != null) {
        EditDebtDialog(
            debt = selectedDebt!!,
            apartments = apartments,
            onDismiss = { 
                showEditDebtDialog = false
                selectedDebt = null
            },
            onConfirm = { apartmentId, amount, description, dueDate ->
                debtViewModel.updateDebt(context, selectedDebt!!.id, apartmentId, amount, description, dueDate)
            }
        )
    }

    if (showDeleteConfirmDialog && debtToDelete != null) {
        ConfirmDialog(
            title = "Eliminar Deuda",
            message = "¿Está seguro que desea eliminar la deuda de $${debtToDelete!!.amount} del apartamento ${debtToDelete!!.apartment_number}? Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            onConfirm = {
                debtViewModel.deleteDebt(context, debtToDelete!!.id)
            },
            onDismiss = {
                showDeleteConfirmDialog = false
                debtToDelete = null
            }
        )
    }
}

@Composable
fun DebtCard(
    debt: Debt,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Header with apartment info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apartamento ${debt.apartment_number}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = "Piso ${debt.floor_number}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Amount with emphasis
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$${debt.amount}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    debt.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    debt.due_date?.let { date ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Vence: ${formatDate(date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Status badge
                val statusColor = when (debt.status) {
                    "pending" -> MaterialTheme.colorScheme.error
                    "paid" -> MaterialTheme.colorScheme.primary
                    "overdue" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                val statusText = when (debt.status) {
                    "pending" -> "Pendiente"
                    "paid" -> "Pagado"
                    "overdue" -> "Vencida"
                    else -> debt.status
                }
                
                Badge(
                    containerColor = statusColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons with improved design
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = if (value.isNotBlank()) formatDate(value) else "",
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text("Seleccionar fecha") },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
            }
        },
        modifier = modifier
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { dateString ->
                onValueChange(dateString)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onDateSelected(formatter.format(date))
                    }
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDebtDialog(
    apartments: List<Apartment>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String?, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var selectedApartmentId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Crear Deuda",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedApartmentId?.let { id ->
                            apartments.find { it.id == id }?.let { "Apt ${it.apartment_number} - Piso ${it.floor_number}" }
                        } ?: "Seleccionar apartamento",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Apartamento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        apartments.forEach { apartment ->
                            DropdownMenuItem(
                                text = { Text("Apt ${apartment.apartment_number} - Piso ${apartment.floor_number}") },
                                onClick = {
                                    selectedApartmentId = apartment.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    placeholder = { Text("100.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Concepto de la deuda") },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = "Fecha de vencimiento (opcional)",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (selectedApartmentId != null && amountValue != null && amountValue > 0) {
                                onConfirm(
                                    selectedApartmentId!!,
                                    amountValue,
                                    description.takeIf { it.isNotBlank() },
                                    dueDate.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = selectedApartmentId != null && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
                    ) {
                        Text("Crear")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDebtDialog(
    debt: Debt,
    apartments: List<Apartment>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String?, String?) -> Unit
) {
    var amount by remember { mutableStateOf(debt.amount.toString()) }
    var description by remember { mutableStateOf(debt.description ?: "") }
    var dueDate by remember { mutableStateOf(debt.due_date ?: "") }
    var selectedApartmentId by remember { mutableStateOf(debt.apartment_id) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar Deuda",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = apartments.find { it.id == selectedApartmentId }?.let { 
                            "Apt ${it.apartment_number} - Piso ${it.floor_number}" 
                        } ?: "Seleccionar apartamento",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Apartamento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        apartments.forEach { apartment ->
                            DropdownMenuItem(
                                text = { Text("Apt ${apartment.apartment_number} - Piso ${apartment.floor_number}") },
                                onClick = {
                                    selectedApartmentId = apartment.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = "Fecha de vencimiento (opcional)",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && amountValue > 0) {
                                onConfirm(
                                    selectedApartmentId,
                                    amountValue,
                                    description.takeIf { it.isNotBlank() },
                                    dueDate.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}