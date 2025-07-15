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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarrod.house.data.model.Debt
import com.jarrod.house.data.model.Payment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onCreateDebt: () -> Unit,
    onViewMetrics: () -> Unit,
    onViewPayments: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Deudas", "Pagos", "Métricas")

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
        }
    }
}

@Composable
fun DebtsTab(onCreateDebt: () -> Unit) {
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

        // Mock debt list
        LazyColumn {
            items(5) { index ->
                DebtCard(
                    debt = Debt(
                        id = index + 1,
                        apartment_id = index + 1,
                        amount = 100.0 + (index * 25),
                        description = "Pago de servicios mes ${index + 1}",
                        due_date = "2024-01-${15 + index}",
                        status = if (index % 2 == 0) "pending" else "paid",
                        created_at = "2024-01-01",
                        apartment_number = "${index + 1}01",
                        floor_number = index + 1
                    ),
                    onEdit = { },
                    onDelete = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PaymentsTab(onViewPayments: () -> Unit) {
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

        LazyColumn {
            items(3) { index ->
                PendingPaymentCard(
                    payment = Payment(
                        id = index + 1,
                        debt_id = index + 1,
                        amount = 100.0 + (index * 25),
                        payment_date = "2024-01-${10 + index}",
                        receipt_path = "receipt_${index + 1}.jpg",
                        status = "pending",
                        approved_by = null,
                        approved_at = null,
                        notes = null,
                        apartment_number = "${index + 1}01",
                        floor_number = index + 1
                    ),
                    onApprove = { },
                    onReject = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun MetricsTab(onViewMetrics: () -> Unit) {
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
                    text = "Resumen de pagos, historial y estadísticas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    onReject: () -> Unit
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