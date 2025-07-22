package com.jarrod.house.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.data.model.Debt
import com.jarrod.house.data.model.Payment
import com.jarrod.house.ui.viewmodel.DebtViewModel
import com.jarrod.house.ui.viewmodel.ApartmentsViewModel
import com.jarrod.house.ui.viewmodel.PaymentViewModel
import com.jarrod.house.ui.viewmodel.UsersViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AdminDashboard(
    onCreateDebt: () -> Unit,
    onViewMetrics: () -> Unit,
    onViewPayments: () -> Unit,
    onManageUsers: () -> Unit,
    onManageApartments: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Deudas", "Pagos", "Métricas", "Gestión")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Enhanced TopAppBar
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Panel Administrador",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )

            // Enhanced TabRow
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = { 
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab content with animated transitions
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut())
                }
            ) { tab ->
                when (tab) {
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
    }
}

@Composable
fun DebtsTab(
    onCreateDebt: () -> Unit,
    debtViewModel: DebtViewModel = viewModel(),
    apartmentsViewModel: ApartmentsViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val debts by debtViewModel.debts.collectAsState()
    val apartments by apartmentsViewModel.apartments.collectAsState()
    val payments by paymentViewModel.payments.collectAsState()
    val isLoading by debtViewModel.isLoading.collectAsState()
    val error by debtViewModel.error.collectAsState()
    val updateResult by debtViewModel.updateResult.collectAsState()
    val createResult by debtViewModel.createResult.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedDebt by remember { mutableStateOf<Debt?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todas", "Pendientes", "Pagadas", "Vencidas")

    // Load debts when tab is displayed
    LaunchedEffect(Unit) {
        debtViewModel.loadDebts(context)
        apartmentsViewModel.loadApartments(context)
        paymentViewModel.loadPayments(context)
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

    // Handle create results
    LaunchedEffect(createResult) {
        createResult?.let { result ->
            if (result.isSuccess) {
                showCreateDialog = false
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
                onClick = { showCreateDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear deuda")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row for filtering
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

        Spacer(modifier = Modifier.height(16.dp))

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
                LazyColumn {
                    items(filteredDebts) { debt ->
                        // Find the associated payment for this debt
                        val associatedPayment = payments.find { payment -> 
                            payment.debt_id == debt.id 
                        }
                        
                        AdminDebtCard(
                            debt = debt,
                            payment = associatedPayment,
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
                onConfirm = { apartmentId: Int, amount: Double, description: String?, dueDate: String? ->
                    debtViewModel.updateDebt(context, selectedDebt!!.id, apartmentId, amount, description, dueDate)
                }
            )
        }

        // Create dialog
        if (showCreateDialog) {
            CreateDebtDialog(
                apartments = apartments,
                onDismiss = { showCreateDialog = false },
                onConfirm = { apartmentId: Int, amount: Double, description: String?, dueDate: String? ->
                    debtViewModel.createDebt(context, apartmentId, amount, description, dueDate)
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

@SuppressLint("DefaultLocale")
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

@Composable
fun AdminDebtCard(
    debt: Debt,
    payment: Payment? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showImagePreview by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
                        modifier = Modifier.padding(bottom = 12.dp)
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
                                style = MaterialTheme.typography.titleMedium,
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
                            modifier = Modifier.padding(bottom = 8.dp)
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
                                text = "Vence: $date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Show payment receipt if debt is paid and payment has receipt
                    if (debt.status == "paid" && payment != null && payment.receipt_path != null) {
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
                                        text = "Comprobante de pago",
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
                
                // Status badge with improved design
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
    
    // Image preview dialog for paid debt receipts
    if (showImagePreview && payment != null && payment.receipt_path != null) {
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
                        "Apt. ${debt.apartment_number} - \$${payment.amount}",
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

@Composable
fun PendingPaymentCard(
    payment: Payment,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Apt. ${payment.apartment_number} - Piso ${payment.floor_number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Monto: $${payment.amount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Fecha: ${payment.payment_date}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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

            Spacer(modifier = Modifier.height(12.dp))

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
                        "Apt. ${payment.apartment_number} - \$${payment.amount}",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDebtDialog(
    debt: Debt,
    apartments: List<Apartment>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String?, String?) -> Unit
) {
    var selectedApartmentId by remember { mutableStateOf(debt.apartment_id) }
    var amount by remember { mutableStateOf(debt.amount.toString()) }
    var description by remember { mutableStateOf(debt.description ?: "") }
    var dueDate by remember { mutableStateOf(debt.due_date ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Deuda") },
        text = {
            Column {
                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Due date field
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Fecha de vencimiento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        selectedApartmentId,
                        amountDouble,
                        description.ifBlank { null },
                        dueDate.ifBlank { null }
                    )
                    onDismiss()
                }
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDebtDialog(
    apartments: List<Apartment>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String?, String?) -> Unit
) {
    var selectedApartmentId by remember { mutableStateOf(apartments.firstOrNull()?.id ?: 0) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nueva Deuda") },
        text = {
            Column {
                // Apartment selection dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = apartments.find { it.id == selectedApartmentId }?.apartment_number ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Apartamento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        apartments.forEach { apartment ->
                            DropdownMenuItem(
                                text = { Text("Apt ${apartment.apartment_number}") },
                                onClick = {
                                    selectedApartmentId = apartment.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Due date field
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Fecha de vencimiento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    if (amountDouble > 0 && selectedApartmentId > 0) {
                        onConfirm(
                            selectedApartmentId,
                            amountDouble,
                            description.ifBlank { null },
                            dueDate.ifBlank { null }
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}