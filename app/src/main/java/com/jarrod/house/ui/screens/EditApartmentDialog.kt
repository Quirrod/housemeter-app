package com.jarrod.house.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jarrod.house.data.model.Apartment
import com.jarrod.house.data.model.Floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditApartmentDialog(
    apartment: Apartment,
    floors: List<Floor>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var apartmentNumber by remember { mutableStateOf(apartment.apartment_number) }
    var meterNumber by remember { mutableStateOf(apartment.meter_number) }
    var selectedFloorId by remember { mutableStateOf(apartment.floor_id) }
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
                    text = "Editar Apartamento",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = floors.find { it.id == selectedFloorId }?.let { "Piso ${it.floor_number}" } ?: "Seleccionar piso",
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
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = meterNumber,
                    onValueChange = { meterNumber = it },
                    label = { Text("Número de medidor") },
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
                            if (apartmentNumber.isNotBlank() && meterNumber.isNotBlank()) {
                                onConfirm(selectedFloorId, apartmentNumber, meterNumber)
                            }
                        },
                        enabled = apartmentNumber.isNotBlank() && meterNumber.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun EditFloorDialog(
    floor: Floor,
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    var floorNumber by remember { mutableStateOf(floor.floor_number.toString()) }
    var description by remember { mutableStateOf(floor.description ?: "") }

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
                    text = "Editar Piso",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = floorNumber,
                    onValueChange = { floorNumber = it },
                    label = { Text("Número de piso") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
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
                        Text("Guardar")
                    }
                }
            }
        }
    }
}