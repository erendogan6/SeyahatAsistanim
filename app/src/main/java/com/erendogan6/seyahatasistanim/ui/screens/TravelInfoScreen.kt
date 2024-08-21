package com.erendogan6.seyahatasistanim.ui.screens

import android.util.Log
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.erendogan6.seyahatasistanim.data.model.weather.City
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
    var departureLatitude by remember { mutableStateOf<Double?>(null) }
    var departureLongitude by remember { mutableStateOf<Double?>(null) }
    var arrivalLatitude by remember { mutableStateOf<Double?>(null) }
    var arrivalLongitude by remember { mutableStateOf<Double?>(null) }

    val departureCityLoadingState by viewModel.departureCityLoadingState.collectAsState()
    val arrivalCityLoadingState by viewModel.arrivalCityLoadingState.collectAsState()

    val isFormValid by remember {
        derivedStateOf {
            departureDate.isNotEmpty() &&
                arrivalDate.isNotEmpty() &&
                departurePlace.isNotEmpty() &&
                arrivalPlace.isNotEmpty() &&
                travelMethod.isNotEmpty() &&
                departureLatitude != null &&
                departureLongitude != null &&
                arrivalLatitude != null &&
                arrivalLongitude != null
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

            customCityField(
                value = departurePlace,
                onValueChange = {
                    departurePlace = it
                    viewModel.fetchDepartureCitySuggestions(it)
                },
                label = "Kalkış Yeri",
                citySuggestions = (departureCityLoadingState as? TravelViewModel.LoadingState.Loaded)?.data ?: emptyList(),
                onCitySelected = { city ->
                    departurePlace =
                        if (city.localNames?.tr != null) {
                            city.localNames.tr + " - " + city.country
                        } else {
                            city.name + " - " + city.country
                        }

                    departureLatitude = city.latitude
                    departureLongitude = city.longitude
                    println(city.latitude)
                },
                loadingState = departureCityLoadingState,
            )

            Spacer(modifier = Modifier.height(16.dp))

            datePickerField(
                value = arrivalDate,
                onValueChange = { arrivalDate = it },
                label = "Varış Tarihi",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            customCityField(
                value = arrivalPlace,
                onValueChange = {
                    arrivalPlace = it
                    viewModel.fetchArrivalCitySuggestions(it)
                },
                label = "Varış Yeri",
                citySuggestions = (arrivalCityLoadingState as? TravelViewModel.LoadingState.Loaded)?.data ?: emptyList(),
                onCitySelected = { city ->
                    arrivalPlace =
                        if (city.localNames?.tr != null) {
                            city.localNames.tr + " - " + city.country
                        } else {
                            city.name + " - " + city.country
                        }
                    arrivalLatitude = city.latitude
                    arrivalLongitude = city.longitude
                },
                loadingState = arrivalCityLoadingState,
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
                                departureLatitude = departureLatitude ?: 0.0,
                                departureLongitude = departureLongitude ?: 0.0,
                                arrivalLatitude = arrivalLatitude ?: 0.0,
                                arrivalLongitude = arrivalLongitude ?: 0.0,
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
fun customCityField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    citySuggestions: List<City>,
    onCitySelected: (City) -> Unit,
    loadingState: TravelViewModel.LoadingState<List<City>>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCityText by remember { mutableStateOf(value) }

    // Watch for changes in loadingState to control the dropdown expansion
    LaunchedEffect(loadingState) {
        expanded = loadingState is TravelViewModel.LoadingState.Loaded && loadingState.data.isNotEmpty()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        TextField(
            value = selectedCityText,
            onValueChange = { text ->
                selectedCityText = text
                onValueChange(text)
                Log.d("CustomCityField", "Query: $text, Suggestions count: ${citySuggestions.size}")
            },
            label = { Text(label) },
            modifier =
                Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        when (loadingState) {
            is TravelViewModel.LoadingState.Loaded -> {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    loadingState.data.forEach { city ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (city.localNames?.tr != null) {
                                        city.localNames.tr + " - " + city.country
                                    } else {
                                        city.name + " - " + city.country
                                    },
                                )
                            },
                            onClick = {
                                onCitySelected(city)
                                selectedCityText =
                                    if (city.localNames?.tr != null) {
                                        city.localNames.tr + " - " + city.country
                                    } else {
                                        city.name + " - " + city.country
                                    }
                                Log.d("CustomCityField", "City clicked: ${city.name}")
                                expanded = false
                            },
                        )
                    }
                }
            }
            else -> {
                expanded = false
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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
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
