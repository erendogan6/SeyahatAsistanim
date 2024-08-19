package com.erendogan6.seyahatasistanim.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(text = "Seyahat Bilgilerini Girin", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        datePickerWithDialog(
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { selectedDate ->
                departureDate = selectedDate
            },
            label = "Kalkış Tarihi",
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = departurePlace,
            onValueChange = { departurePlace = it },
            label = { Text("Kalkış Yeri") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        datePickerWithDialog(
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { selectedDate ->
                arrivalDate = selectedDate
            },
            label = "Varış Tarihi",
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = arrivalPlace,
            onValueChange = { arrivalPlace = it },
            label = { Text("Varış Yeri") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        travelMethodDropdown(travelMethod) {
            travelMethod = it
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Devam Et")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun datePickerWithDialog(
    modifier: Modifier = Modifier,
    onDateSelected: (String) -> Unit,
    label: String,
) {
    val dateState = rememberDatePickerState()
    val millisToLocalDate =
        dateState.selectedDateMillis?.let {
            DateUtils().convertMillisToLocalDate(it)
        }
    val dateToString =
        millisToLocalDate?.let {
            DateUtils().dateToString(millisToLocalDate)
        } ?: label
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        showDialog = true
                    }),
            text = dateToString,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            millisToLocalDate?.let { date ->
                                onDateSelected(DateUtils().dateToString(date))
                            }
                        },
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false },
                    ) {
                        Text(text = "Cancel")
                    }
                },
            ) {
                DatePicker(
                    state = dateState,
                    showModeToggle = true,
                )
            }
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
        onExpandedChange = {
            expanded = !expanded
        },
    ) {
        TextField(
            value = selectedMethod,
            onValueChange = { },
            readOnly = true,
            label = { Text("Seyahat Vasıtası") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
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
