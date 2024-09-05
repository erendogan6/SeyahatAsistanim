package com.erendogan6.seyahatasistanim.presentation.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.data.local.database.TravelDatabase
import com.erendogan6.seyahatasistanim.data.model.dto.weather.City
import com.erendogan6.seyahatasistanim.data.model.dto.weather.LocalNames
import com.erendogan6.seyahatasistanim.data.model.entity.TravelEntity
import com.erendogan6.seyahatasistanim.domain.usecase.GetCitySuggestionsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetLastTravelInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveTravelInfoUseCase
import com.erendogan6.seyahatasistanim.utils.isNetworkAvailable
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TravelViewModelTest {
    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val saveTravelInfoUseCase: SaveTravelInfoUseCase = mockk()
    private val getLastTravelInfoUseCase: GetLastTravelInfoUseCase = mockk()
    private val getCitySuggestionsUseCase: GetCitySuggestionsUseCase = mockk()
    private val context: Context = mockk(relaxed = true)
    private val database: TravelDatabase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TravelViewModel
    private lateinit var chatGptViewModel: ChatGptViewModel
    private lateinit var weatherViewModel: WeatherViewModel

    private val travelEntity =
        TravelEntity(
            id = 0,
            departureDate = "10 October 2024",
            arrivalDate = "15 October 2024",
            departurePlace = "Paris",
            arrivalPlace = "New York",
            travelMethod = "Plane",
            departureLatitude = 48.8566,
            departureLongitude = 2.3522,
            arrivalLatitude = 40.7128,
            arrivalLongitude = -74.0060,
            daysToStay = 5,
        )

    @get:Rule
    val testWatcher =
        object : TestWatcher() {
            override fun starting(description: Description?) {
                super.starting(description)
                mockkStatic(Log::class)
                every { Log.d(any(), any()) } returns 0
                every { Log.e(any(), any()) } returns 0
            }
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        chatGptViewModel = mockk(relaxed = true)
        weatherViewModel = mockk(relaxed = true)

        mockWeatherDataFlows()
        mockNetworkConnectivity()

        viewModel =
            TravelViewModel(
                saveTravelInfoUseCase,
                getLastTravelInfoUseCase,
                getCitySuggestionsUseCase,
                context,
                database,
                testDispatcher,
            )

        mockContextResources()
    }

    private fun mockWeatherDataFlows() {
        every { weatherViewModel.weatherData } returns MutableStateFlow(null)
        every { chatGptViewModel.localInfo } returns MutableStateFlow(null)
        every { chatGptViewModel.checklistItems } returns MutableStateFlow(emptyList())
    }

    private fun mockNetworkConnectivity() {
        val connectivityManager = mockk<ConnectivityManager>()
        val networkCapabilities = mockk<NetworkCapabilities>()

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { context.getString(R.string.no_internet_connection) } returns "No internet connection"

        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.getNetworkCapabilities(any()) } returns null
        every { networkCapabilities.hasTransport(any()) } returns false

        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
    }

    private fun mockContextResources() {
        every { context.getString(R.string.no_internet_connection) } returns "No internet connection"
        every { context.getString(R.string.weather_data_received, any()) } returns "Weather data received"
        every { context.getString(R.string.local_info_received, any()) } returns "Local info received"
        every { context.getString(R.string.checklist_created, any()) } returns "Checklist created"
        every { context.getString(R.string.fetch_departure_city_suggestions, any()) } returns "Fetching departure city suggestions"
        every { context.getString(R.string.fetch_arrival_city_suggestions, any()) } returns "Fetching arrival city suggestions"
        every { context.getString(R.string.failed_to_load_suggestions) } returns "Failed to load suggestions"
        every { context.getString(R.string.query_is_blank) } returns "Query is blank"
        every { context.getString(R.string.load_last_travel_info) } returns "Loading last travel info"
        every { context.getString(R.string.last_travel_info_loaded) } returns "Last travel info loaded"
        every { context.getString(R.string.delete_travel_info) } returns "Deleting travel info"
        every { context.getString(R.string.travel_info_deleted) } returns "Travel info deleted"
        every { context.getString(R.string.error_fetching_suggestions) } returns "Error fetching suggestions"
        every { context.getString(R.string.unknown_error_occurred) } returns "Unknown error occurred"
        every { context.getString(R.string.travel_info_saved_to_db) } returns "Travel info saved"
    }

    @Test
    fun `saveTravelInfo should handle no internet connection gracefully`() =
        runTest {
            // Arrange
            coEvery { saveTravelInfoUseCase(any()) } just Runs

            // Act
            viewModel.saveTravelInfo(travelEntity, mockk(), mockk()) {}

            advanceUntilIdle()

            // Assert
            assertThat(viewModel.errorState.value).isEqualTo("No internet connection")
            assertThat(viewModel.isLoading.value).isFalse()
        }

    @Test
    fun `saveTravelInfo should proceed when network is available`() =
        runTest {
            // Arrange
            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns true
            coEvery { saveTravelInfoUseCase.invoke(travelEntity) } just Runs

            // Act
            viewModel.saveTravelInfo(travelEntity, mockk(), mockk()) {}

            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { saveTravelInfoUseCase.invoke(eq(travelEntity)) }
            assertThat(viewModel.travelInfo.value).isNotNull()
            assertThat(viewModel.travelInfo.value).isEqualTo(travelEntity)
            assertThat(viewModel.isLoading.value).isFalse()
        }

    @Test
    fun `loadLastTravelInfo should load last travel info successfully`() =
        runTest {
            // Arrange
            coEvery { getLastTravelInfoUseCase() } returns travelEntity

            // Act
            viewModel.loadLastTravelInfo()
            advanceUntilIdle()

            // Assert
            coVerify { getLastTravelInfoUseCase.invoke() }
            assertThat(viewModel.travelInfo.value).isNotNull()
            assertThat(viewModel.travelInfo.value).isEqualTo(travelEntity)
        }

    @Test
    fun `deleteTravelInfo should clear all travel data from the database`() =
        runTest {
            // Arrange
            coEvery { database.clearAllTables() } just Runs

            // Act
            viewModel.deleteTravelInfo {}
            advanceUntilIdle()

            // Assert
            coVerify { database.clearAllTables() }
            assertThat(viewModel.travelInfo.value).isNull()
        }

    @Test
    fun `saveTravelInfo should trigger loading of weather and local info`() =
        runTest {
            // Arrange
            val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH) // Ensure fixed Locale
            val parsedDate = LocalDate.parse(travelEntity.arrivalDate, dateFormatter)

            // Mock network availability
            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns true

            coEvery { saveTravelInfoUseCase.invoke(travelEntity) } just Runs

            // Act
            viewModel.saveTravelInfo(travelEntity, chatGptViewModel, weatherViewModel) {}

            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) {
                weatherViewModel.fetchWeatherData(
                    eq(travelEntity.arrivalLatitude),
                    eq(travelEntity.arrivalLongitude),
                    eq(parsedDate),
                    eq(travelEntity.daysToStay),
                )
            }
        }

    @Test
    fun `fetchCitySuggestions should fetch departure suggestions when query is not blank`() =
        runTest {
            // Arrange
            val cityList =
                listOf(
                    City("Paris", 26.0, 35.0, "FR", LocalNames("Paris", "Paris")),
                    City("London", 26.0, 35.0, "FR", LocalNames("Paris", "Paris")),
                )
            coEvery { getCitySuggestionsUseCase(any()) } returns flowOf(cityList)

            // Act
            viewModel.fetchCitySuggestions("Par", isDeparture = true)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.departureCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded(cityList))
            coVerify { getCitySuggestionsUseCase.invoke("Par") }
        }

    @Test
    fun `fetchCitySuggestions should fetch arrival suggestions when query is not blank`() =
        runTest {
            // Arrange
            val cityList =
                listOf(
                    City("Istanbul", 41.0, 28.0, "TR", LocalNames("Istanbul", "İstanbul")),
                    City("Ankara", 39.9, 32.8, "TR", LocalNames("Ankara", "Ankara")),
                )
            coEvery { getCitySuggestionsUseCase(any()) } returns flowOf(cityList)

            // Act
            viewModel.fetchCitySuggestions("ank", isDeparture = false)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.arrivalCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded(cityList))
            coVerify { getCitySuggestionsUseCase.invoke("ank") }
        }

    @Test
    fun `fetchCitySuggestions should handle empty query for departure gracefully`() =
        runTest {
            // Act
            viewModel.fetchCitySuggestions("", isDeparture = true)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.departureCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded<List<City>>(emptyList()))
            coVerify(exactly = 0) { getCitySuggestionsUseCase.invoke(any()) }
        }

    @Test
    fun `fetchCitySuggestions should handle empty query for arrival gracefully`() =
        runTest {
            // Act
            viewModel.fetchCitySuggestions("", isDeparture = false)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.arrivalCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded<List<City>>(emptyList()))
            coVerify(exactly = 0) { getCitySuggestionsUseCase.invoke(any()) }
        }

    @Test
    fun `fetchCitySuggestions should handle error during fetching for departure`() =
        runTest {
            // Arrange
            val exceptionMessage = "Error fetching suggestions"
            coEvery { getCitySuggestionsUseCase(any()) } throws Exception(exceptionMessage)

            // Act
            viewModel.fetchCitySuggestions("London", isDeparture = true)
            advanceUntilIdle()

            // Assert
            val state = viewModel.departureCityLoadingState.value
            assertThat(state).isInstanceOf(TravelViewModel.LoadingState.Error::class.java)
            assertThat((state as TravelViewModel.LoadingState.Error).message).isEqualTo("Failed to load suggestions")
        }

    @Test
    fun `fetchCitySuggestions should handle error during fetching for arrival`() =
        runTest {
            // Arrange
            val exceptionMessage = "Network error"
            coEvery { getCitySuggestionsUseCase(any()) } throws Exception(exceptionMessage)

            // Act
            viewModel.fetchCitySuggestions("ank", isDeparture = false)
            advanceUntilIdle()

            // Assert
            val state = viewModel.arrivalCityLoadingState.value
            assertThat(state).isInstanceOf(TravelViewModel.LoadingState.Error::class.java)
            assertThat((state as TravelViewModel.LoadingState.Error).message).isEqualTo("Failed to load suggestions")
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
}
