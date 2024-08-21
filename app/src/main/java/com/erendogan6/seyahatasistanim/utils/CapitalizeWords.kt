package com.erendogan6.seyahatasistanim.utils

import java.util.Locale

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") {
        it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
