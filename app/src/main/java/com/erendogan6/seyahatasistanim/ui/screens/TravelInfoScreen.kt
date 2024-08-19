package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity
import com.erendogan6.seyahatasistanim.ui.viewmodel.TravelViewModel
import com.erendogan6.seyahatasistanim.utils.DateUtils
import org.koin.androidx.compose.koinViewModel

@Composable
fun travelInfoScreen(
    navController: NavController,
    viewModel: TravelViewModel = koinViewModel(),
) {
    var departureDate by remember { mutableStateOf("") }
    var arrivalDate by remember { mutableStateOf("") }
    var departurePlace by remember { mutableStateOf("") }
    var arrivalPlace by remember { mutableStateOf("") }
    var travelMethod by remember { mutableStateOf("") }
    val isFormValid by remember {
        derivedStateOf {
            departureDate.isNotEmpty() &&
                arrivalDate.isNotEmpty() &&
                departurePlace.isNotEmpty() &&
                arrivalPlace.isNotEmpty() &&
                travelMethod.isNotEmpty()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Lütfen Seyahat Bilgilerini Girin",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            datePickerField(
                value = departureDate,
                onValueChange = { departureDate = it },
                label = "Kalkış Tarihi",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            customTextField(
                value = departurePlace,
                onValueChange = { departurePlace = it },
                label = "Kalkış Yeri",
            )
            Spacer(modifier = Modifier.height(16.dp))

            datePickerField(
                value = arrivalDate,
                onValueChange = { arrivalDate = it },
                label = "Varış Tarihi",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            customTextField(
                value = arrivalPlace,
                onValueChange = { arrivalPlace = it },
                label = "Varış Yeri",
            )

            Spacer(modifier = Modifier.height(16.dp))

            travelMethodDropdown(travelMethod) {
                travelMethod = it
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        val travelEntity =
                            TravelEntity(
                                departureDate = departureDate,
                                arrivalDate = arrivalDate,
                                departurePlace = departurePlace,
                                arrivalPlace = arrivalPlace,
                                travelMethod = travelMethod,
                            )
                        viewModel.saveTravelInfo(travelEntity)
                        navController.navigate("home")
                    }
                },
                enabled = isFormValid,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(55.dp)
                        .shadow(4.dp, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f),
                    ),
            ) {
                Text(text = "Devam Et", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun datePickerField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .clickable { showDialog = true },
    ) {
        if (value.isEmpty()) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp),
            )
        } else {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp),
            )
        }
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = DateUtils().convertMillisToLocalDate(millis)
                            onValueChange(DateUtils().dateToString(localDate))
                        }
                        showDialog = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                ) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun travelMethodDropdown(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val travelMethods = listOf("Otobüs", "Uçak", "Tren", "Araba")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            value = selectedMethod,
            onValueChange = { },
            readOnly = true,
            label = {
                Text(
                    text = "Seyahat Vasıtası",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors =
                TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            travelMethods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(text = method) },
                    onClick = {
                        onMethodSelected(method)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun customTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors =
            TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
    )
}
