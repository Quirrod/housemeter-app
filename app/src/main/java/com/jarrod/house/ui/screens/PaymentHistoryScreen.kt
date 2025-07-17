package com.jarrod.house.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import com.jarrod.house.data.model.Payment
import com.jarrod.house.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onBackClick: () -> Unit,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val userPayments by userViewModel.userPayments.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todos", "Pendientes", "Aprobados", "Rechazados")

    // Load payments when screen starts
    LaunchedEffect(Unit) {
        userViewModel.loadUserPayments(context)
    }

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
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Historial de Pagos",
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
                val filteredPayments = when (selectedTab) {
                    0 -> userPayments
                    1 -> userPayments.filter { it.status == "pending" }
                    2 -> userPayments.filter { it.status == "approved" }
                    3 -> userPayments.filter { it.status == "rejected" }
                    else -> userPayments
                }

                if (filteredPayments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                when (selectedTab) {
                                    1 -> Icons.Default.PendingActions
                                    2 -> Icons.Default.CheckCircle
                                    3 -> Icons.Default.Cancel
                                    else -> Icons.Default.History
                                },
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = when (selectedTab) {
                                    1 -> "No tienes pagos pendientes"
                                    2 -> "No tienes pagos aprobados"
                                    3 -> "No tienes pagos rechazados"
                                    else -> "No has realizado ningún pago"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (selectedTab) {
                                    1 -> "Los pagos pendientes aparecerán aquí mientras esperan aprobación"
                                    2 -> "Los pagos aprobados por el administrador aparecerán aquí"
                                    3 -> "Los pagos rechazados aparecerán aquí con la razón del rechazo"
                                    else -> "Cuando realices pagos, aparecerán en esta sección"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Summary statistics for current filter
                    if (selectedTab == 0 && userPayments.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                PaymentSummaryCard(userPayments)
                            }
                            
                            items(filteredPayments.sortedByDescending { it.payment_date }) { payment ->
                                PaymentHistoryDetailCard(payment = payment)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredPayments.sortedByDescending { it.payment_date }) { payment ->
                                PaymentHistoryDetailCard(payment = payment)
                            }
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
                        TextButton(onClick = { userViewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(payments: List<Payment>) {
    val totalPayments = payments.size
    val totalAmount = payments.sumOf { it.amount }
    val approvedPayments = payments.count { it.status == "approved" }
    val pendingPayments = payments.count { it.status == "pending" }
    val rejectedPayments = payments.count { it.status == "rejected" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Resumen de Pagos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "Total",
                    value = totalPayments.toString(),
                    icon = Icons.Default.Payment,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SummaryItem(
                    title = "Aprobados",
                    value = approvedPayments.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryItem(
                    title = "Pendientes",
                    value = pendingPayments.toString(),
                    icon = Icons.Default.PendingActions,
                    color = MaterialTheme.colorScheme.tertiary
                )
                SummaryItem(
                    title = "Rechazados",
                    value = rejectedPayments.toString(),
                    icon = Icons.Default.Cancel,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total pagado:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$${String.format("%.2f", totalAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (totalPayments > 0) {
                val approvalRate = (approvedPayments.toFloat() / totalPayments * 100)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { approvalRate / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tasa de aprobación: ${String.format("%.1f", approvalRate)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PaymentHistoryDetailCard(payment: Payment) {
    val context = LocalContext.current
    var showImagePreview by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    // Payment description
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = payment.debt_description ?: "Pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Amount with emphasis
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (payment.status) {
                                "approved" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                "pending" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                "rejected" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
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
                                tint = when (payment.status) {
                                    "approved" -> MaterialTheme.colorScheme.primary
                                    "pending" -> MaterialTheme.colorScheme.tertiary
                                    "rejected" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$${payment.amount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when (payment.status) {
                                    "approved" -> MaterialTheme.colorScheme.primary
                                    "pending" -> MaterialTheme.colorScheme.tertiary
                                    "rejected" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                    
                    // Payment date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fecha: ${payment.payment_date}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Approval date if approved
                    if (payment.approved_at != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Aprobado: ${payment.approved_at}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Receipt indicator with preview/download
                    if (payment.receipt_path != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Comprobante adjunto",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Preview button
                                    OutlinedButton(
                                        onClick = { showImagePreview = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Vista previa",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    
                                    // Download button
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                downloadReceipt(context, payment.receipt_path!!)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(8.dp),
                                        enabled = !isDownloading
                                    ) {
                                        if (isDownloading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Download,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (isDownloading) "Descargando..." else "Descargar",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Status badge
                val statusColor = when (payment.status) {
                    "approved" -> MaterialTheme.colorScheme.primary
                    "pending" -> MaterialTheme.colorScheme.tertiary
                    "rejected" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surface
                }
                
                val statusText = when (payment.status) {
                    "approved" -> "APROBADO"
                    "pending" -> "PENDIENTE"
                    "rejected" -> "RECHAZADO"
                    else -> payment.status.uppercase()
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

            // Additional info for rejected payments
            if (payment.status == "rejected") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Este pago fue rechazado. Puedes contactar al administrador para más información.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    // Image preview dialog
    if (showImagePreview && payment.receipt_path != null) {
        Dialog(
            onDismissRequest = { showImagePreview = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Comprobante de pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showImagePreview = false }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "${payment.debt_description ?: "Pago"} - \$${payment.amount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = getReceiptUrl(payment.receipt_path!!),
                            contentDescription = "Comprobante de pago",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

// Helper function to build the receipt URL
private fun getReceiptUrl(receiptPath: String): String {
    return "https://housemeter-backend-production.up.railway.app/uploads/$receiptPath"
}

// Download function
private suspend fun downloadReceipt(context: android.content.Context, receiptPath: String) {
    try {
        val url = getReceiptUrl(receiptPath)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle download error
        e.printStackTrace()
    }
}