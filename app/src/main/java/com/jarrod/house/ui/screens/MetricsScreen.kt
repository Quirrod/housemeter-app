package com.jarrod.house.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarrod.house.ui.viewmodel.DebtViewModel
import com.jarrod.house.ui.viewmodel.PaymentViewModel
import com.jarrod.house.ui.viewmodel.UsersViewModel
import com.jarrod.house.ui.viewmodel.ApartmentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun MetricsScreen(
    onBackClick: () -> Unit,
    debtViewModel: DebtViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel(),
    apartmentsViewModel: ApartmentsViewModel = viewModel()
) {
    val context = LocalContext.current
    val debts by debtViewModel.debts.collectAsState()
    val payments by paymentViewModel.payments.collectAsState()
    val users by usersViewModel.users.collectAsState()
    val apartments by apartmentsViewModel.apartments.collectAsState()
    val isLoading by debtViewModel.isLoading.collectAsState()

    // Load data when screen starts
    LaunchedEffect(Unit) {
        debtViewModel.loadDebts(context)
        paymentViewModel.loadPayments(context)
        usersViewModel.loadUsers(context)
        apartmentsViewModel.loadApartments(context)
    }

    // Calculate comprehensive metrics
    val totalDebts = debts.size
    val pendingDebts = debts.count { it.status == "pending" }
    val paidDebts = debts.count { it.status == "paid" }
    val overdueDebts = debts.count { it.status == "overdue" }
    val totalDebtAmount = debts.sumOf { it.amount }
    val pendingDebtAmount = debts.filter { it.status == "pending" }.sumOf { it.amount }
    val paidDebtAmount = debts.filter { it.status == "paid" }.sumOf { it.amount }
    
    val totalPayments = payments.size
    val pendingPayments = payments.count { it.status == "pending" }
    val approvedPayments = payments.count { it.status == "approved" }
    val rejectedPayments = payments.count { it.status == "rejected" }
    val totalPaymentAmount = payments.sumOf { it.amount }
    val approvedPaymentAmount = payments.filter { it.status == "approved" }.sumOf { it.amount }
    
    val totalUsers = users.size
    val adminUsers = users.count { it.role == "admin" }
    val regularUsers = users.count { it.role == "user" }
    val totalApartments = apartments.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Métricas Detalladas",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Summary Cards Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryCard(
                                title = "Total Deudas",
                                value = totalDebts.toString(),
                                icon = Icons.Default.Receipt,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Total Usuarios",
                                value = totalUsers.toString(),
                                icon = Icons.Default.Person,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Apartamentos",
                                value = totalApartments.toString(),
                                icon = Icons.Default.Business,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        // Debt metrics
                        MetricCard(
                            title = "Análisis de Deudas",
                            icon = Icons.Default.Receipt,
                            iconColor = MaterialTheme.colorScheme.primary
                        ) {
                            MetricRow("Total de deudas", totalDebts.toString())
                            MetricRow("Deudas pendientes", pendingDebts.toString(), MaterialTheme.colorScheme.error)
                            MetricRow("Deudas pagadas", paidDebts.toString(), MaterialTheme.colorScheme.primary)
                            MetricRow("Deudas vencidas", overdueDebts.toString(), MaterialTheme.colorScheme.error)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            MetricRow("Monto total", "$${String.format("%.2f", totalDebtAmount)}", MaterialTheme.colorScheme.onSurface, true)
                            MetricRow("Monto pendiente", "$${String.format("%.2f", pendingDebtAmount)}", MaterialTheme.colorScheme.error, true)
                            MetricRow("Monto cobrado", "$${String.format("%.2f", paidDebtAmount)}", MaterialTheme.colorScheme.primary, true)
                            
                            if (totalDebts > 0) {
                                val paidPercentage = (paidDebts.toFloat() / totalDebts * 100)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { paidPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    text = "Progreso de cobro: ${String.format("%.1f", paidPercentage)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    item {
                        // Payment metrics
                        MetricCard(
                            title = "Análisis de Pagos",
                            icon = Icons.Default.Payment,
                            iconColor = MaterialTheme.colorScheme.secondary
                        ) {
                            MetricRow("Total de pagos", totalPayments.toString())
                            MetricRow("Pagos pendientes", pendingPayments.toString(), MaterialTheme.colorScheme.tertiary)
                            MetricRow("Pagos aprobados", approvedPayments.toString(), MaterialTheme.colorScheme.primary)
                            MetricRow("Pagos rechazados", rejectedPayments.toString(), MaterialTheme.colorScheme.error)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            MetricRow("Monto total", "$${String.format("%.2f", totalPaymentAmount)}", MaterialTheme.colorScheme.onSurface, true)
                            MetricRow("Monto aprobado", "$${String.format("%.2f", approvedPaymentAmount)}", MaterialTheme.colorScheme.primary, true)

                            if (totalPayments > 0) {
                                val approvedPercentage = (approvedPayments.toFloat() / totalPayments * 100)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { approvedPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Tasa de aprobación: ${String.format("%.1f", approvedPercentage)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    item {
                        // User and apartment metrics
                        MetricCard(
                            title = "Gestión del Sistema",
                            icon = Icons.Default.Dashboard,
                            iconColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            MetricRow("Total usuarios", totalUsers.toString())
                            MetricRow("Administradores", adminUsers.toString(), MaterialTheme.colorScheme.primary)
                            MetricRow("Usuarios regulares", regularUsers.toString(), MaterialTheme.colorScheme.secondary)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            MetricRow("Total apartamentos", totalApartments.toString())
                            
                            if (totalApartments > 0 && regularUsers > 0) {
                                val occupancyRate = (regularUsers.toFloat() / totalApartments * 100)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { occupancyRate / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Tasa de ocupación: ${String.format("%.1f", occupancyRate)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    item {
                        // Financial overview
                        MetricCard(
                            title = "Resumen Financiero",
                            icon = Icons.Default.AccountBalance,
                            iconColor = MaterialTheme.colorScheme.primary
                        ) {
                            val pendingRevenue = pendingDebtAmount
                            val collectedRevenue = approvedPaymentAmount
                            val totalRevenue = pendingRevenue + collectedRevenue

                            MetricRow("Ingresos totales esperados", "$${String.format("%.2f", totalRevenue)}", MaterialTheme.colorScheme.onSurface, true)
                            MetricRow("Ingresos cobrados", "$${String.format("%.2f", collectedRevenue)}", MaterialTheme.colorScheme.primary, true)
                            MetricRow("Ingresos pendientes", "$${String.format("%.2f", pendingRevenue)}", MaterialTheme.colorScheme.error, true)

                            if (totalRevenue > 0) {
                                val collectionRate = (collectedRevenue / totalRevenue * 100)
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Eficiencia de Cobro",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "${String.format("%.1f", collectionRate)}%",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (emphasized) 4.dp else 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (emphasized) FontWeight.Medium else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}