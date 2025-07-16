package com.jarrod.house.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.data.model.Debt
import com.jarrod.house.ui.viewmodel.ApartmentsViewModel
import com.jarrod.house.ui.viewmodel.DebtViewModel

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
    var selectedDebt by remember { mutableStateOf<Debt?>(null) }
    
    val debts by debtViewModel.debts.collectAsState()
    val apartments by apartmentsViewModel.apartments.collectAsState()
    val isLoading by debtViewModel.isLoading.collectAsState()
    val error by debtViewModel.error.collectAsState()
    val createDebtResult by debtViewModel.createResult.collectAsState()
    val updateDebtResult by debtViewModel.updateResult.collectAsState()

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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(debts) { debt ->
                    DebtCard(
                        debt = debt,
                        onEdit = { 
                            selectedDebt = debt
                            showEditDebtDialog = true
                        },
                        onDelete = { 
                            debtViewModel.deleteDebt(context, debt.id)
                        }
                    )
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
}

@Composable
fun DebtCard(
    debt: Debt,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Apartamento ${debt.apartment_number}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Piso ${debt.floor_number}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Monto: $${debt.amount}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    debt.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    debt.due_date?.let { date ->
                        Text(
                            text = "Vence: $date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Status badge
                    val statusColor = when (debt.status) {
                        "pending" -> MaterialTheme.colorScheme.error
                        "paid" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    val statusText = when (debt.status) {
                        "pending" -> "Pendiente"
                        "paid" -> "Pagado"
                        else -> debt.status
                    }
                    
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
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

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Fecha de vencimiento (opcional)") },
                    placeholder = { Text("YYYY-MM-DD") },
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

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Fecha de vencimiento (opcional)") },
                    placeholder = { Text("YYYY-MM-DD") },
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