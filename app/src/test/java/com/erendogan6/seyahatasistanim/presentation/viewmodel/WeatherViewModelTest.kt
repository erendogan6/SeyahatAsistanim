package com.erendogan6.seyahatasistanim.presentation.viewmodel

import android.content.Context
import android.util.Log
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.data.model.dto.weather.Temperature
import com.erendogan6.seyahatasistanim.data.model.dto.weather.Weather
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherForecast
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.domain.usecase.GetWeatherDataUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetWeatherForecastUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveWeatherDataUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    // Mocks and dependencies
    private lateinit var viewModel: WeatherViewModel
    private val getWeatherForecastUseCase: GetWeatherForecastUseCase = mockk()
    private val saveWeatherDataUseCase: SaveWeatherDataUseCase = mockk()
    private val getWeatherDataUseCase: GetWeatherDataUseCase = mockk()
    private val context: Context = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            WeatherViewModel(
                getWeatherForecastUseCase,
                saveWeatherDataUseCase,
                getWeatherDataUseCase,
                context,
            )
    }

    @Before
    fun setUpMocks() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0

        every { context.getString(R.string.skipping_api_call, any()) } returns "Skipping API call"
        every { context.getString(R.string.fetching_weather_data, any(), any(), any()) } returns "Fetching weather data"
        every { context.getString(R.string.error_fetching_weather_data_api, any()) } returns "Error fetching weather data"
        every { context.getString(R.string.weather_data_fetched) } returns "Weather data fetched"
        every { context.getString(R.string.weather_data_saved_to_db) } returns "Weather data saved to database"
        every { context.getString(R.string.loading_weather_data_from_db, any()) } returns "Loading weather data from database"
        every { context.getString(R.string.weather_data_loaded_from_db, any()) } returns "Weather data loaded from database"
        every { context.getString(R.string.error_loading_weather_data) } returns "Error loading weather data"
    }

    @Test
    fun `fetchWeatherData should fetch data from API and save to database`() =
        runTest(testDispatcher) {
            // Arrange
            val singleForecast = createWeatherForecast()
            val weatherApiResponse = WeatherApiResponse(forecastList = listOf(singleForecast))
            coEvery { getWeatherForecastUseCase.invoke(40.0, 29.0) } returns flowOf(weatherApiResponse)
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { getWeatherForecastUseCase.invoke(40.0, 29.0) }
            coVerify(exactly = 1) { saveWeatherDataUseCase.invoke(any()) }
            assertThat(viewModel.weatherData.value).isNotNull
            assertThat(viewModel.weatherData.value!!.forecastList).isNotEmpty
        }

    @Test
    fun `loadWeatherFromDb should fetch weather data from database`() =
        runTest {
            // Arrange
            val weatherEntity = WeatherEntity("1", LocalDate.now(), 20.0, 15.0, "Clear")
            coEvery { getWeatherDataUseCase.invoke(any(), any()) } returns listOf(weatherEntity)

            // Act
            viewModel.loadWeatherFromDb(LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            val expectedStartDate = LocalDate.now().minusDays(1)
            val expectedEndDate = LocalDate.now().plusDays(3)

            coVerify(exactly = 1) { getWeatherDataUseCase.invoke(expectedStartDate, expectedEndDate) }
            assertThat(viewModel.weatherFromDb.value).isNotNull
            assertThat(viewModel.weatherFromDb.value).isNotEmpty
        }

    @Test
    fun `fetchWeatherData should load data from database when API fails`() =
        runTest(testDispatcher) {
            // Arrange
            val travelDate = LocalDate.of(2024, 9, 3)
            val daysToStay = 4
            val weatherEntity = WeatherEntity("1", LocalDate.now(), 20.0, 15.0, "Clear")
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } throws Exception("Network Error")
            coEvery { getWeatherDataUseCase.invoke(any(), any()) } returns listOf(weatherEntity)

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, travelDate, daysToStay)
            advanceUntilIdle()

            // Assert
            val expectedStartDate = travelDate.minusDays(1)
            val expectedEndDate = travelDate.plusDays(daysToStay.toLong())

            coVerify(exactly = 1) { getWeatherDataUseCase.invoke(expectedStartDate, expectedEndDate) }
        }

    @Test
    fun `fetchWeatherData should skip API call when travel date is more than 30 days away`() =
        runTest(testDispatcher) {
            // Arrange
            val travelDate = LocalDate.now().plusDays(35)
            val daysToStay = 4

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, travelDate, daysToStay)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNull()
            coVerify(exactly = 0) { getWeatherForecastUseCase.invoke(any(), any()) }
            coVerify(exactly = 0) { getWeatherDataUseCase.invoke(any(), any()) }
        }

    @Test
    fun `loadWeatherFromDb should handle exception gracefully`() =
        runTest {
            // Arrange
            coEvery { getWeatherDataUseCase.invoke(any(), any()) } throws Exception("Database Error")

            // Act
            viewModel.loadWeatherFromDb(LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherFromDb.value).isNull()
            coVerify(exactly = 1) { getWeatherDataUseCase.invoke(any(), any()) }
        }

    @Test
    fun `fetchWeatherData should handle invalid input gracefully`() =
        runTest(testDispatcher) {
            // Act
            viewModel.fetchWeatherData(-999.0, -999.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNull()
            coVerify(exactly = 0) { getWeatherForecastUseCase.invoke(any(), any()) }
        }

    @Test
    fun `fetchWeatherData should handle daysToStay equals zero correctly`() =
        runTest(testDispatcher) {
            // Arrange
            val travelDate = LocalDate.now()
            val singleForecast = createWeatherForecast()
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } returns
                flowOf(WeatherApiResponse(forecastList = listOf(singleForecast)))
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, travelDate, 0)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNotNull()
            assertThat(viewModel.weatherData.value?.forecastList).hasSize(1)
            assertThat(
                viewModel.weatherData.value
                    ?.forecastList
                    ?.first(),
            ).isEqualTo(singleForecast)
            coVerify(exactly = 1) { getWeatherForecastUseCase.invoke(any(), any()) }
            coVerify(exactly = 1) { saveWeatherDataUseCase.invoke(any()) }
        }

    @Test
    fun `fetchWeatherData should handle maximum days to stay correctly`() =
        runTest(testDispatcher) {
            // Arrange
            val travelDate = LocalDate.now()
            val maxDaysToStay = 30
            val temperature = Temperature(20.0, 15.0)
            val weather = listOf(Weather("Clear"))
            val weatherApiResponse = createWeatherApiResponse(travelDate, maxDaysToStay, temperature, weather)
            coEvery { getWeatherForecastUseCase.invoke(40.0, 29.0) } returns flowOf(weatherApiResponse)
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, travelDate, maxDaysToStay)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { getWeatherForecastUseCase.invoke(40.0, 29.0) }
            coVerify(exactly = 1) { saveWeatherDataUseCase.invoke(any()) }
            assertThat(viewModel.weatherData.value).isNotNull
            assertThat(viewModel.weatherData.value!!.forecastList).hasSize(maxDaysToStay + 1)
        }

    @Test
    fun `fetchWeatherData should handle negative days to stay gracefully`() =
        runTest(testDispatcher) {
            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), -5)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNull()
            coVerify(exactly = 0) { getWeatherForecastUseCase.invoke(any(), any()) }
        }

    @Test
    fun `fetchWeatherData should handle edge coordinates correctly`() =
        runTest(testDispatcher) {
            // Arrange
            val weatherApiResponse = WeatherApiResponse(forecastList = emptyList())
            coEvery { getWeatherForecastUseCase.invoke(90.0, 135.0) } returns flowOf(weatherApiResponse)
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(90.0, 135.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 2) { getWeatherForecastUseCase.invoke(90.0, 135.0) }
        }

    @Test
    fun `fetchWeatherData should maintain state after exception`() =
        runTest(testDispatcher) {
            // Arrange
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } throws Exception("API Error")
            coEvery { getWeatherDataUseCase.invoke(any(), any()) } returns emptyList()

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNull()
            assertThat(viewModel.weatherFromDb.value).isNullOrEmpty()
        }

    @Test
    fun `fetchWeatherData should log correct messages on success`() =
        runTest(testDispatcher) {
            // Arrange
            val singleForecast = createWeatherForecast()
            val weatherApiResponse = WeatherApiResponse(forecastList = listOf(singleForecast))
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } returns flowOf(weatherApiResponse)
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            coVerify { Log.i("WeatherViewModel", "Weather data fetched") }
            coVerify(exactly = 1) { getWeatherForecastUseCase.invoke(any(), any()) }
            coVerify(exactly = 1) { saveWeatherDataUseCase.invoke(any()) }
        }

    @Test
    fun `fetchWeatherData should handle empty API response gracefully`() =
        runTest(testDispatcher) {
            // Arrange
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } returns flowOf(WeatherApiResponse(forecastList = emptyList()))
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNull()
            coVerify(exactly = 2) { getWeatherForecastUseCase.invoke(any(), any()) }
            coVerify(exactly = 0) { saveWeatherDataUseCase.invoke(any()) }
        }

    @Test
    fun `fetchWeatherData should handle null API response gracefully`() =
        runTest(testDispatcher) {
            // Arrange
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } returns flowOf<WeatherApiResponse?>(null)

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNull()
            coVerify(exactly = 2) { getWeatherForecastUseCase.invoke(any(), any()) }
        }

    @Test
    fun `fetchWeatherData should handle past travel date gracefully`() =
        runTest(testDispatcher) {
            // Arrange
            val pastDate = LocalDate.now().minusDays(1)

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, pastDate, 3)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value).isNull()
        }

    @Test
    fun `loadWeatherFromDb should handle no data in database`() =
        runTest(testDispatcher) {
            // Arrange
            coEvery { getWeatherDataUseCase.invoke(any(), any()) } returns emptyList()

            // Act
            viewModel.loadWeatherFromDb(LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherFromDb.value).isNullOrEmpty()
        }

    @Test
    fun `fetchWeatherData should handle correct time zone data`() =
        runTest(testDispatcher) {
            // Arrange
            val travelDate = LocalDate.now()
            val weatherApiResponse = createWeatherApiResponse(travelDate, 0, Temperature(20.0, 15.0), listOf(Weather("Clear")))
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } returns flowOf(weatherApiResponse)
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, travelDate, 0)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value?.forecastList).hasSize(1)
            coVerify(exactly = 1) { getWeatherForecastUseCase.invoke(any(), any()) }
            coVerify(exactly = 1) { saveWeatherDataUseCase.invoke(any()) }
        }

    @Test
    fun `fetchWeatherData should handle multiple API failures and retry`() =
        runTest(testDispatcher) {
            // Arrange
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } throws Exception("Network Error")
            coEvery { getWeatherDataUseCase.invoke(any(), any()) } returns emptyList()

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 2) { getWeatherForecastUseCase.invoke(any(), any()) }
            assertThat(viewModel.weatherFromDb.value).isNullOrEmpty()
        }

    @Test
    fun `fetchWeatherData should not save invalid API data to database`() =
        runTest(testDispatcher) {
            // Arrange
            val invalidApiResponse = WeatherApiResponse(forecastList = emptyList())
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } returns flowOf(invalidApiResponse)
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 2) { getWeatherForecastUseCase.invoke(any(), any()) }
            coVerify(exactly = 0) { saveWeatherDataUseCase.invoke(any()) }
        }

    @Test
    fun `fetchWeatherData should log appropriate error messages on API failure`() =
        runTest(testDispatcher) {
            // Arrange
            val errorMessage = "Network Error"
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } throws Exception(errorMessage)

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, LocalDate.now(), 3)
            advanceUntilIdle()

            // Assert
            coVerify { Log.e("WeatherViewModel", "Unexpected error: $errorMessage") }
        }

    @Test
    fun `fetchWeatherData should handle large dataset gracefully`() =
        runTest(testDispatcher) {
            // Arrange
            val travelDate = LocalDate.now()
            val largeDataSet = createLargeWeatherForecastList(travelDate, 1000)
            val weatherApiResponse = WeatherApiResponse(forecastList = largeDataSet)
            coEvery { getWeatherForecastUseCase.invoke(any(), any()) } returns flowOf(weatherApiResponse)
            coEvery { saveWeatherDataUseCase.invoke(any()) } just Runs

            // Act
            viewModel.fetchWeatherData(40.0, 29.0, travelDate, 30)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.weatherData.value?.forecastList).hasSize(31)
            coVerify(exactly = 1) { getWeatherForecastUseCase.invoke(any(), any()) }
        }

    // Helper functions
    private fun createLargeWeatherForecastList(
        startDate: LocalDate,
        count: Int,
    ): List<WeatherForecast> =
        List(count) { index ->
            WeatherForecast(
                dateTime = startDate.plusDays(index.toLong()).toEpochDay() * (24 * 60 * 60),
                temp = Temperature(20.0, 15.0),
                weather = listOf(Weather("Clear")),
            )
        }

    // Helper functions
    private fun createWeatherForecast(): WeatherForecast =
        WeatherForecast(
            dateTime = LocalDate.now().toEpochDay() * (24 * 60 * 60),
            temp = Temperature(20.0, 15.0),
            weather = listOf(Weather("Clear")),
        )

    private fun createWeatherApiResponse(
        travelDate: LocalDate,
        days: Int,
        temperature: Temperature,
        weather: List<Weather>,
    ): WeatherApiResponse =
        WeatherApiResponse(
            forecastList =
                List(days + 1) { index ->
                    WeatherForecast(
                        dateTime = travelDate.plusDays(index.toLong()).toEpochDay() * (24 * 60 * 60),
                        temp = temperature,
                        weather = weather,
                    )
                },
        )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
}
