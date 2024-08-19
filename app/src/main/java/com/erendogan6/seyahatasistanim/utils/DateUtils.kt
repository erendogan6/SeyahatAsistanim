package com.erendogan6.seyahatasistanim.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateUtils {
    fun convertMillisToLocalDate(millis: Long): LocalDate =
        Instant
            .ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    fun dateToString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        return date.format(formatter)
    }
}
