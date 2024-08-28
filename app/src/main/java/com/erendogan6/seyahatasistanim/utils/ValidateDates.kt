package com.erendogan6.seyahatasistanim.utils

import android.content.Context
import com.erendogan6.seyahatasistanim.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun validateDates(
    context: Context,
    departureDate: String,
    arrivalDate: String,
    onError: (String?) -> Unit,
) {
    if (departureDate.isNotEmpty() && arrivalDate.isNotEmpty()) {
        try {
            val departure = LocalDate.parse(departureDate, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
            val arrival = LocalDate.parse(arrivalDate, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))

            when {
                arrival.isBefore(departure) -> {
                    onError(context.getString(R.string.error_arrival_date_earlier))
                }
                departure.isAfter(arrival) -> {
                    onError(context.getString(R.string.error_departure_date_later))
                }
                else -> {
                    onError(null)
                }
            }
        } catch (e: Exception) {
            onError(context.getString(R.string.error_invalid_date_format))
        }
    } else {
        onError(null)
    }
}
