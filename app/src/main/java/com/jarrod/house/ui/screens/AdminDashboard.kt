package com.jarrod.house.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarrod.house.data.model.Debt
import com.jarrod.house.data.model.Payment
import com.jarrod.house.ui.viewmodel.DebtViewModel
import com.jarrod.house.ui.viewmodel.ApartmentsViewModel
import com.jarrod.house.ui.viewmodel.UsersViewModel
import com.jarrod.house.ui.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onCreateDebt: () -> Unit,
    onViewMetrics: () -> Unit,
    onViewPayments: () -> Unit,
    onManageUsers: () -> Unit,
    onManageApartments: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Deudas", "Pagos", "Métricas", "Gestión")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Panel Administrador") },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> DebtsTab(onCreateDebt = onCreateDebt)
            1 -> PaymentsTab(onViewPayments = onViewPayments)
            2 -> MetricsTab(onViewMetrics = onViewMetrics)
            3 -> ManagementTab(
                onManageUsers = onManageUsers,
                onManageApartments = onManageApartments
            )
        }
    }
}

@Composable
fun DebtsTab(
    onCreateDebt: () -> Unit,
    debtViewModel: DebtViewModel = viewModel(),
    apartmentsViewModel: ApartmentsViewModel = viewModel()
) {
    val context = LocalContext.current
    val debts by debtViewModel.debts.collectAsState()
    val apartments by apartmentsViewModel.apartments.collectAsState()
    val isLoading by debtViewModel.isLoading.collectAsState()
    val error by debtViewModel.error.collectAsState()
    val updateResult by debtViewModel.updateResult.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedDebt by remember { mutableStateOf<Debt?>(null) }

    // Load debts when tab is displayed
    LaunchedEffect(Unit) {
        debtViewModel.loadDebts(context)
        apartmentsViewModel.loadApartments(context)
    }

    // Handle update results
    LaunchedEffect(updateResult) {
        updateResult?.let { result ->
            if (result.isSuccess) {
                showEditDialog = false
                selectedDebt = null
                debtViewModel.clearCreateResult()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gestión de Deudas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = onCreateDebt,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear deuda")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(debts) { debt ->
                    DebtCard(
                        debt = debt,
                        onEdit = { 
                            selectedDebt = debt
                            showEditDialog = true
                        },
                        onDelete = { 
                            debtViewModel.deleteDebt(context, debt.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Error handling
        error?.let { errorMsg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
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

        // Edit dialog
        if (showEditDialog && selectedDebt != null) {
            EditDebtDialog(
                debt = selectedDebt!!,
                apartments = apartments,
                onDismiss = { 
                    showEditDialog = false
                    selectedDebt = null
                },
                onConfirm = { apartmentId, amount, description, dueDate ->
                    debtViewModel.updateDebt(context, selectedDebt!!.id, apartmentId, amount, description, dueDate)
                }
            )
        }
    }
}

@Composable
fun PaymentsTab(
    onViewPayments: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val payments by paymentViewModel.payments.collectAsState()
    val isLoading by paymentViewModel.isLoading.collectAsState()
    val error by paymentViewModel.error.collectAsState()
    val updateResult by paymentViewModel.updateResult.collectAsState()

    // Load payments when tab is displayed
    LaunchedEffect(Unit) {
        paymentViewModel.loadPayments(context)
    }

    // Handle update results
    LaunchedEffect(updateResult) {
        updateResult?.let { result ->
            if (result.isSuccess) {
                paymentViewModel.clearUpdateResult()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pagos Pendientes de Aprobación",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(payments.filter { it.status == "pending" }) { payment ->
                    PendingPaymentCard(
                        payment = payment,
                        onApprove = { 
                            paymentViewModel.updatePaymentStatus(
                                context, 
                                payment.id, 
                                "approved", 
                                null
                            )
                        },
                        onReject = { 
                            paymentViewModel.updatePaymentStatus(
                                context, 
                                payment.id, 
                                "rejected", 
                                null
                            )
                        },
                        onDelete = {
                            paymentViewModel.deletePayment(context, payment.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Error handling
        error?.let { errorMsg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(errorMsg, modifier = Modifier.weight(1f))
                    TextButton(onClick = { paymentViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsTab(
    onViewMetrics: () -> Unit,
    debtViewModel: DebtViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val debts by debtViewModel.debts.collectAsState()
    val payments by paymentViewModel.payments.collectAsState()
    val isLoading by debtViewModel.isLoading.collectAsState()

    // Load data when tab is displayed
    LaunchedEffect(Unit) {
        debtViewModel.loadDebts(context)
        paymentViewModel.loadPayments(context)
    }

    // Calculate metrics
    val totalDebts = debts.size
    val pendingDebts = debts.count { it.status == "pending" }
    val paidDebts = debts.count { it.status == "paid" }
    val totalDebtAmount = debts.sumOf { it.amount }
    val pendingDebtAmount = debts.filter { it.status == "pending" }.sumOf { it.amount }
    val pendingPayments = payments.count { it.status == "pending" }
    val approvedPayments = payments.count { it.status == "approved" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Métricas y Reportes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Debt metrics
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resumen de Deudas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total de deudas: $totalDebts")
                            Text("Deudas pendientes: $pendingDebts")
                            Text("Deudas pagadas: $paidDebts")
                            Text("Monto total: $${String.format("%.2f", totalDebtAmount)}")
                            Text("Monto pendiente: $${String.format("%.2f", pendingDebtAmount)}")
                        }
                    }
                }

                item {
                    // Payment metrics
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resumen de Pagos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pagos pendientes: $pendingPayments")
                            Text("Pagos aprobados: $approvedPayments")
                        }
                    }
                }

                item {
                    // Detailed metrics card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onViewMetrics
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ver Métricas Detalladas",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reportes avanzados, historial y estadísticas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Apt. ${debt.apartment_number} - Piso ${debt.floor_number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = when (debt.status) {
                        "paid" -> MaterialTheme.colorScheme.primary
                        "pending" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(debt.status.uppercase())
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = debt.description ?: "Sin descripción",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Monto: $${debt.amount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            if (debt.due_date != null) {
                Text(
                    text = "Vencimiento: ${debt.due_date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun PendingPaymentCard(
    payment: Payment,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Apt. ${payment.apartment_number} - Piso ${payment.floor_number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Monto: $${payment.amount}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = "Fecha: ${payment.payment_date}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (payment.receipt_path != null) {
                Text(
                    text = "Comprobante: ${payment.receipt_path}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                onDelete?.let { deleteCallback ->
                    TextButton(
                        onClick = deleteCallback,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                OutlinedButton(onClick = onReject) {
                    Text("Rechazar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onApprove) {
                    Text("Aprobar")
                }
            }
        }
    }
}

@Composable
fun ManagementTab(
    onManageUsers: () -> Unit,
    onManageApartments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestión del Sistema",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onManageUsers
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gestionar Usuarios",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crear, editar y eliminar usuarios del sistema",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onManageApartments
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Business, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gestionar Propiedades",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Administrar pisos, apartamentos y medidores",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}