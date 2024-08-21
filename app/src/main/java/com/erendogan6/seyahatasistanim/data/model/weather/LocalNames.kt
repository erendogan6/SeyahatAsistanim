package com.erendogan6.seyahatasistanim.data.model.weather

import com.google.gson.annotations.SerializedName

data class LocalNames(
    @SerializedName("tr") val tr: String? = null,
    @SerializedName("en") val en: String? = null,
)
