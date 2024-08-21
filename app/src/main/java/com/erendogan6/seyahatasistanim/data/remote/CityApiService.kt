package com.erendogan6.seyahatasistanim.data.remote

import com.erendogan6.seyahatasistanim.data.model.weather.City
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApiService {
    @GET("geo/1.0/direct")
    suspend fun getCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
    ): List<City>
}
