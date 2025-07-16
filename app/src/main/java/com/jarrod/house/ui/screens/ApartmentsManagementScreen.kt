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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.data.model.Floor
import com.jarrod.house.ui.viewmodel.ApartmentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentsManagementScreen(
    onBackClick: () -> Unit,
    viewModel: ApartmentsViewModel = viewModel()
) {
    val context = LocalContext.current
    var showCreateApartmentDialog by remember { mutableStateOf(false) }
    var showCreateFloorDialog by remember { mutableStateOf(false) }
    var showEditApartmentDialog by remember { mutableStateOf(false) }
    var showEditFloorDialog by remember { mutableStateOf(false) }
    var selectedApartment by remember { mutableStateOf<Apartment?>(null) }
    var selectedFloor by remember { mutableStateOf<Floor?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Apartamentos", "Pisos")
    
    val apartments by viewModel.apartments.collectAsState()
    val floors by viewModel.floors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val createApartmentResult by viewModel.createApartmentResult.collectAsState()
    val createFloorResult by viewModel.createFloorResult.collectAsState()
    val updateApartmentResult by viewModel.updateApartmentResult.collectAsState()
    val updateFloorResult by viewModel.updateFloorResult.collectAsState()

    // Load data when screen starts
    LaunchedEffect(Unit) {
        viewModel.loadApartments(context)
        viewModel.loadFloors(context)
    }

    // Handle create results
    LaunchedEffect(createApartmentResult) {
        createApartmentResult?.let { result ->
            if (result.isSuccess) {
                showCreateApartmentDialog = false
                viewModel.clearCreateResults()
            }
        }
    }

    LaunchedEffect(createFloorResult) {
        createFloorResult?.let { result ->
            if (result.isSuccess) {
                showCreateFloorDialog = false
                viewModel.clearCreateResults()
            }
        }
    }

    LaunchedEffect(updateApartmentResult) {
        updateApartmentResult?.let { result ->
            if (result.isSuccess) {
                showEditApartmentDialog = false
                selectedApartment = null
                viewModel.clearCreateResults()
            }
        }
    }

    LaunchedEffect(updateFloorResult) {
        updateFloorResult?.let { result ->
            if (result.isSuccess) {
                showEditFloorDialog = false
                selectedFloor = null
                viewModel.clearCreateResults()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Gestión de Propiedades") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                IconButton(onClick = { 
                    if (selectedTab == 0) showCreateApartmentDialog = true
                    else showCreateFloorDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Crear")
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
            0 -> ApartmentsTab(
                apartments = apartments,
                isLoading = isLoading,
                onEditApartment = { apartment ->
                    selectedApartment = apartment
                    showEditApartmentDialog = true
                },
                onDeleteApartment = { apartmentId ->
                    viewModel.deleteApartment(context, apartmentId)
                }
            )
            1 -> FloorsTab(
                floors = floors,
                isLoading = isLoading,
                onEditFloor = { floor ->
                    selectedFloor = floor
                    showEditFloorDialog = true
                },
                onDeleteFloor = { floorId ->
                    viewModel.deleteFloor(context, floorId)
                }
            )
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
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            }
        }
    }

    if (showCreateApartmentDialog) {
        CreateApartmentDialog(
            floors = floors,
            onDismiss = { showCreateApartmentDialog = false },
            onConfirm = { floorId, apartmentNumber, meterNumber ->
                viewModel.createApartment(context, floorId, apartmentNumber, meterNumber)
            }
        )
    }

    if (showCreateFloorDialog) {
        CreateFloorDialog(
            onDismiss = { showCreateFloorDialog = false },
            onConfirm = { floorNumber, description ->
                viewModel.createFloor(context, floorNumber, description)
            }
        )
    }

    if (showEditApartmentDialog && selectedApartment != null) {
        EditApartmentDialog(
            apartment = selectedApartment!!,
            floors = floors,
            onDismiss = { 
                showEditApartmentDialog = false
                selectedApartment = null
            },
            onConfirm = { floorId, apartmentNumber, meterNumber ->
                viewModel.updateApartment(context, selectedApartment!!.id, floorId, apartmentNumber, meterNumber)
            }
        )
    }

    if (showEditFloorDialog && selectedFloor != null) {
        EditFloorDialog(
            floor = selectedFloor!!,
            onDismiss = { 
                showEditFloorDialog = false
                selectedFloor = null
            },
            onConfirm = { floorNumber, description ->
                viewModel.updateFloor(context, selectedFloor!!.id, floorNumber, description)
            }
        )
    }
}

@Composable
fun ApartmentsTab(
    apartments: List<Apartment>,
    isLoading: Boolean,
    onEditApartment: (Apartment) -> Unit,
    onDeleteApartment: (Int) -> Unit
) {
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
            items(apartments) { apartment ->
                ApartmentCard(
                    apartment = apartment,
                    onEdit = { 
                        onEditApartment(apartment)
                    },
                    onDelete = { 
                        onDeleteApartment(apartment.id)
                    }
                )
            }
        }
    }
}

@Composable
fun FloorsTab(
    floors: List<Floor>,
    isLoading: Boolean,
    onEditFloor: (Floor) -> Unit,
    onDeleteFloor: (Int) -> Unit
) {
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
            items(floors) { floor ->
                FloorCard(
                    floor = floor,
                    onEdit = { 
                        onEditFloor(floor)
                    },
                    onDelete = { 
                        onDeleteFloor(floor.id)
                    }
                )
            }
        }
    }
}

@Composable
fun ApartmentCard(
    apartment: Apartment,
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
                        text = "Apartamento ${apartment.apartment_number}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Piso ${apartment.floor_number}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Medidor: ${apartment.meter_number}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.Home,
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

@Composable
fun FloorCard(
    floor: Floor,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Piso ${floor.floor_number}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (floor.description != null) {
                        Text(
                            text = floor.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(
                    Icons.Default.Business,
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
fun CreateApartmentDialog(
    floors: List<Floor>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var apartmentNumber by remember { mutableStateOf("") }
    var meterNumber by remember { mutableStateOf("") }
    var selectedFloorId by remember { mutableStateOf<Int?>(null) }
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
                    text = "Crear Apartamento",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFloorId?.let { id ->
                            floors.find { it.id == id }?.let { "Piso ${it.floor_number}" }
                        } ?: "Seleccionar piso",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Piso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        floors.forEach { floor ->
                            DropdownMenuItem(
                                text = { Text("Piso ${floor.floor_number}") },
                                onClick = {
                                    selectedFloorId = floor.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = apartmentNumber,
                    onValueChange = { apartmentNumber = it },
                    label = { Text("Número de apartamento") },
                    placeholder = { Text("101, 102, etc.") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = meterNumber,
                    onValueChange = { meterNumber = it },
                    label = { Text("Número de medidor") },
                    placeholder = { Text("M001, M002, etc.") },
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
                            if (selectedFloorId != null && apartmentNumber.isNotBlank() && meterNumber.isNotBlank()) {
                                onConfirm(selectedFloorId!!, apartmentNumber, meterNumber)
                            }
                        },
                        enabled = selectedFloorId != null && apartmentNumber.isNotBlank() && meterNumber.isNotBlank()
                    ) {
                        Text("Crear")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateFloorDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    var floorNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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
                    text = "Crear Piso",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = floorNumber,
                    onValueChange = { floorNumber = it },
                    label = { Text("Número de piso") },
                    placeholder = { Text("1, 2, 3, etc.") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Primer piso, Segundo piso, etc.") },
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
                            val floor = floorNumber.toIntOrNull()
                            if (floor != null) {
                                onConfirm(floor, description.takeIf { it.isNotBlank() })
                            }
                        },
                        enabled = floorNumber.toIntOrNull() != null
                    ) {
                        Text("Crear")
                    }
                }
            }
        }
    }
}

