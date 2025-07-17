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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarrod.house.data.model.User
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.ui.viewmodel.UsersViewModel
import com.jarrod.house.ui.viewmodel.ApartmentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersManagementScreen(
    onBackClick: () -> Unit,
    usersViewModel: UsersViewModel = viewModel(),
    apartmentsViewModel: ApartmentsViewModel = viewModel()
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    
    val users by usersViewModel.users.collectAsState()
    val apartments by apartmentsViewModel.apartments.collectAsState()
    val isLoading by usersViewModel.isLoading.collectAsState()
    val error by usersViewModel.error.collectAsState()
    val createResult by usersViewModel.createResult.collectAsState()
    val updateResult by usersViewModel.updateResult.collectAsState()

    // Load data when screen starts
    LaunchedEffect(Unit) {
        usersViewModel.loadUsers(context)
        apartmentsViewModel.loadApartments(context)
    }

    // Handle create result
    LaunchedEffect(createResult) {
        createResult?.let { result ->
            if (result.isSuccess) {
                showCreateDialog = false
                usersViewModel.clearCreateResult()
            }
        }
    }

    // Handle update result
    LaunchedEffect(updateResult) {
        updateResult?.let { result ->
            if (result.isSuccess) {
                showEditDialog = false
                selectedUser = null
                usersViewModel.clearUpdateResult()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Gestión de Usuarios") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Crear usuario")
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
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
                    items(users) { user ->
                        UserCard(
                            user = user,
                            apartments = apartments,
                            onEdit = { 
                                selectedUser = user
                                showEditDialog = true
                            },
                            onDelete = { 
                                usersViewModel.deleteUser(context, user.id)
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
                    TextButton(onClick = { usersViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            apartments = apartments,
            onDismiss = { showCreateDialog = false },
            onConfirm = { username, password, role, apartmentId ->
                usersViewModel.createUser(context, username, password, role, apartmentId)
            }
        )
    }

    if (showEditDialog && selectedUser != null) {
        EditUserDialog(
            user = selectedUser!!,
            apartments = apartments,
            onDismiss = { 
                showEditDialog = false
                selectedUser = null
            },
            onConfirm = { username: String, role: String, apartmentId: Int? ->
                usersViewModel.updateUser(context, selectedUser!!.id, username, null, role, apartmentId)
            }
        )
    }
}

@Composable
fun UserCard(
    user: User,
    apartments: List<Apartment>,
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
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Badge(
                        containerColor = when (user.role) {
                            "admin" -> MaterialTheme.colorScheme.primary
                            "user" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Text(user.role.uppercase())
                    }
                    
                    if (user.apartment_id != null) {
                        val apartment = apartments.find { it.id == user.apartment_id }
                        if (apartment != null) {
                            Text(
                                text = "Apartamento ${apartment.apartment_number} - Piso ${apartment.floor_number}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
fun CreateUserDialog(
    apartments: List<Apartment>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int?) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") }
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
                    text = "Crear Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Role selection
                Column {
                    Text("Rol:", style = MaterialTheme.typography.bodyMedium)
                    Row {
                        RadioButton(
                            selected = selectedRole == "user",
                            onClick = { selectedRole = "user" }
                        )
                        Text("Usuario", modifier = Modifier.padding(start = 8.dp))
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        RadioButton(
                            selected = selectedRole == "admin",
                            onClick = { selectedRole = "admin" }
                        )
                        Text("Administrador", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                // Apartment selection (only for users)
                if (selectedRole == "user") {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedApartmentId?.let { id ->
                                apartments.find { it.id == id }?.apartment_number ?: ""
                            } ?: "Seleccionar apartamento",
                            onValueChange = {},
                            readOnly = true,
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
                                    text = { Text("${apartment.apartment_number} - Piso ${apartment.floor_number}") },
                                    onClick = {
                                        selectedApartmentId = apartment.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

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
                            if (username.isNotBlank() && password.isNotBlank()) {
                                onConfirm(username, password, selectedRole, selectedApartmentId)
                            }
                        },
                        enabled = username.isNotBlank() && password.isNotBlank()
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
fun EditUserDialog(
    user: User,
    apartments: List<Apartment>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int?) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var selectedRole by remember { mutableStateOf(user.role) }
    var selectedApartmentId by remember { mutableStateOf(user.apartment_id) }
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
                    text = "Editar Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Role selection
                Column {
                    Text("Rol:", style = MaterialTheme.typography.bodyMedium)
                    Row {
                        RadioButton(
                            selected = selectedRole == "user",
                            onClick = { 
                                selectedRole = "user"
                                if (selectedRole == "admin") {
                                    selectedApartmentId = null
                                }
                            }
                        )
                        Text("Usuario", modifier = Modifier.padding(start = 8.dp))
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        RadioButton(
                            selected = selectedRole == "admin",
                            onClick = { 
                                selectedRole = "admin"
                                selectedApartmentId = null
                            }
                        )
                        Text("Administrador", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                // Apartment selection (only for users)
                if (selectedRole == "user") {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedApartmentId?.let { id ->
                                apartments.find { it.id == id }?.apartment_number ?: ""
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
                                    text = { Text("${apartment.apartment_number} - Piso ${apartment.floor_number}") },
                                    onClick = {
                                        selectedApartmentId = apartment.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

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
                            if (username.isNotBlank()) {
                                onConfirm(username, selectedRole, selectedApartmentId)
                            }
                        },
                        enabled = username.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

