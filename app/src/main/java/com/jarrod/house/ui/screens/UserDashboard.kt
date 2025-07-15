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
fun UserDashboard(
    onPayDebt: (Debt) -> Unit,
    onViewPaymentHistory: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mis Deudas", "Historial")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Mi Apartamento") },
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
            0 -> MyDebtsTab(onPayDebt = onPayDebt)
            1 -> PaymentHistoryTab(onViewPaymentHistory = onViewPaymentHistory)
        }
    }
}

@Composable
fun MyDebtsTab(onPayDebt: (Debt) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Deudas Pendientes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mock debt list for user
        val userDebts = listOf(
            Debt(
                id = 1,
                apartment_id = 1,
                amount = 125.0,
                description = "Pago de servicios enero",
                due_date = "2024-01-15",
                status = "pending",
                created_at = "2024-01-01",
                apartment_number = "101",
                floor_number = 1
            ),
            Debt(
                id = 2,
                apartment_id = 1,
                amount = 150.0,
                description = "Pago de servicios febrero",
                due_date = "2024-02-15",
                status = "overdue",
                created_at = "2024-02-01",
                apartment_number = "101",
                floor_number = 1
            )
        )

        if (userDebts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡No tienes deudas pendientes!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Estás al día con todos tus pagos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(userDebts) { debt ->
                    UserDebtCard(
                        debt = debt,
                        onPay = { onPayDebt(debt) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryTab(onViewPaymentHistory: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Pagos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mock payment history
        val paymentHistory = listOf(
            Payment(
                id = 1,
                debt_id = 1,
                amount = 100.0,
                payment_date = "2024-01-10",
                receipt_path = "receipt_1.jpg",
                status = "approved",
                approved_by = 1,
                approved_at = "2024-01-11",
                notes = null,
                debt_description = "Pago de servicios diciembre"
            ),
            Payment(
                id = 2,
                debt_id = 2,
                amount = 110.0,
                payment_date = "2024-01-08",
                receipt_path = "receipt_2.jpg",
                status = "pending",
                approved_by = null,
                approved_at = null,
                notes = null,
                debt_description = "Pago de servicios noviembre"
            )
        )

        LazyColumn {
            items(paymentHistory) { payment ->
                PaymentHistoryCard(payment = payment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun UserDebtCard(
    debt: Debt,
    onPay: () -> Unit
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
                        text = debt.description ?: "Pago pendiente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Monto: $${debt.amount}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (debt.due_date != null) {
                        Text(
                            text = "Vencimiento: ${debt.due_date}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (debt.status == "overdue") 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Badge(
                    containerColor = when (debt.status) {
                        "overdue" -> MaterialTheme.colorScheme.error
                        "pending" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        when (debt.status) {
                            "overdue" -> "VENCIDA"
                            "pending" -> "PENDIENTE"
                            else -> debt.status.uppercase()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPay,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pagar Ahora")
            }
        }
    }
}

@Composable
fun PaymentHistoryCard(payment: Payment) {
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
                        text = payment.debt_description ?: "Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "$${payment.amount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Fecha: ${payment.payment_date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (payment.approved_at != null) {
                        Text(
                            text = "Aprobado: ${payment.approved_at}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Badge(
                    containerColor = when (payment.status) {
                        "approved" -> MaterialTheme.colorScheme.primary
                        "pending" -> MaterialTheme.colorScheme.tertiary
                        "rejected" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        when (payment.status) {
                            "approved" -> "APROBADO"
                            "pending" -> "PENDIENTE"
                            "rejected" -> "RECHAZADO"
                            else -> payment.status.uppercase()
                        }
                    )
                }
            }

            if (payment.receipt_path != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Comprobante adjunto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}