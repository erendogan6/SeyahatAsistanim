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
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
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

        viewModel =
            TravelViewModel(
                saveTravelInfoUseCase,
                getLastTravelInfoUseCase,
                getCitySuggestionsUseCase,
                context,
                database,
                testDispatcher,
                testDispatcher,
            )

        mockWeatherDataFlows()
        mockNetworkConnectivity()
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
        every { context.getString(R.string.weather_data_received) } returns "Weather data received"
        every { context.getString(R.string.local_info_received) } returns "Local info received"
        every { context.getString(R.string.checklist_created) } returns "Checklist created"
        every { context.getString(R.string.fetch_departure_city_suggestions) } returns "Fetching departure city suggestions"
        every { context.getString(R.string.fetch_arrival_city_suggestions) } returns "Fetching arrival city suggestions"
    }

    @Test
    fun `saveTravelInfo should handle no internet connection gracefully`() =
        runTest {
            // Arrange
            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns false

            coEvery { saveTravelInfoUseCase.invoke(any()) } just Runs

            // Act
            viewModel.saveTravelInfo(travelEntity, mockk(), mockk()) {}

            advanceUntilIdle()

            // Assert
            assertThat(viewModel.errorState.value).isEqualTo("No internet connection")
            assertThat(viewModel.isLoading.value).isFalse()
            coVerify(exactly = 0) { saveTravelInfoUseCase.invoke(any()) }
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
            val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
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

    @Test
    fun `fetchCitySuggestions should debounce calls`() =
        runTest {
            // Arrange
            val cityList = listOf(City("Berlin", 52.52, 13.405, "DE", LocalNames("Berlin", "Berlin")))
            coEvery { getCitySuggestionsUseCase(any()) } returns flowOf(cityList)

            // Act
            viewModel.fetchCitySuggestions("Ber", isDeparture = true)
            viewModel.fetchCitySuggestions("Berl", isDeparture = true)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.departureCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded(cityList))
            coVerify(exactly = 1) { getCitySuggestionsUseCase.invoke("Berl") }
        }

    @Test
    fun `fetchCitySuggestions should handle rapid sequential calls gracefully`() =
        runTest {
            // Act
            viewModel.fetchCitySuggestions("Pa", isDeparture = true)
            viewModel.fetchCitySuggestions("Par", isDeparture = true)
            viewModel.fetchCitySuggestions("Paris", isDeparture = true)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 3) { getCitySuggestionsUseCase.invoke("Paris") }
            coVerify(exactly = 0) { getCitySuggestionsUseCase.invoke("Pa") }
            coVerify(exactly = 0) { getCitySuggestionsUseCase.invoke("Par") }
        }

    @Test
    fun `fetchCitySuggestions should display loading state when fetching suggestions`() =
        runTest {
            // Arrange
            val cityList =
                listOf(
                    City("Paris", 26.0, 35.0, "FR", LocalNames("Paris", "Paris")),
                    City("London", 26.0, 35.0, "FR", LocalNames("London", "Londres")),
                )

            coEvery { getCitySuggestionsUseCase(any()) } returns flowOf(cityList)

            // Act
            viewModel.fetchCitySuggestions("Par", isDeparture = true)

            // Assert initial state is Loading
            assertThat(viewModel.departureCityLoadingState.value)
                .isInstanceOf(TravelViewModel.LoadingState.Loading::class.java)

            // Simulate debounce delay
            advanceTimeBy(300)

            // Allow all coroutines to finish
            advanceUntilIdle()

            // Assert final state is Loaded
            assertThat(viewModel.departureCityLoadingState.value)
                .isEqualTo(TravelViewModel.LoadingState.Loaded(cityList))

            // Verify the use case was called
            coVerify { getCitySuggestionsUseCase.invoke("Par") }
        }

    @Test
    fun `fetchCitySuggestions should handle non-existent city gracefully`() =
        runTest {
            // Arrange
            coEvery { getCitySuggestionsUseCase(any()) } returns flowOf(emptyList())

            // Act
            viewModel.fetchCitySuggestions("Atlantis", isDeparture = false)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.arrivalCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded<List<City>>(emptyList()))
            coVerify { getCitySuggestionsUseCase.invoke("Atlantis") }
        }

    @Test
    fun `saveTravelInfo should not proceed when network is unavailable`() =
        runTest {
            // Arrange
            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns false

            // Act
            viewModel.saveTravelInfo(travelEntity, chatGptViewModel, weatherViewModel) {}

            // Assert
            assertThat(viewModel.errorState.value).isEqualTo("No internet connection")
            coVerify(exactly = 0) { saveTravelInfoUseCase.invoke(any()) }
        }

    @Test
    fun `saveTravelInfo should handle exceptions during save use case`() =
        runTest {
            // Arrange
            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns true
            val exceptionMessage = "Database error"
            coEvery { saveTravelInfoUseCase.invoke(travelEntity) } throws Exception(exceptionMessage)

            // Act
            viewModel.saveTravelInfo(travelEntity, chatGptViewModel, weatherViewModel) {}
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.errorState.value).isEqualTo(exceptionMessage)
            assertThat(viewModel.isLoading.value).isFalse()
            coVerify(exactly = 1) { saveTravelInfoUseCase.invoke(travelEntity) }
        }

    @Test
    fun `deleteTravelInfo should handle exceptions gracefully`() =
        runTest {
            // Arrange
            val exceptionMessage = "Deletion failed"

            // Mock the behavior of the delete operation to throw an exception
            coEvery { database.clearAllTables() } throws Exception(exceptionMessage)

            // Ensure that getLastTravelInfoUseCase is mocked properly to avoid unexpected calls
            coEvery { getLastTravelInfoUseCase.invoke() } returns null // Bu mocklamayı ekleyin

            // Act
            viewModel.deleteTravelInfo {
                fail("The completion callback should not be triggered")
            }

            // Beklemenin doğru şekilde yapılmasını sağlayın
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.errorState.value).isEqualTo(exceptionMessage)
            assertThat(viewModel.travelInfo.value).isNull()

            // Verify that the mocked methods were called
            coVerify { database.clearAllTables() }
        }

    @Test
    fun `fetchCitySuggestions should retry on intermittent network error`() =
        runTest {
            // Arrange
            val retryableException = Exception("Temporary network issue")
            coEvery { getCitySuggestionsUseCase(any()) } throws retryableException andThen
                flowOf(listOf(City("Rome", 41.9028, 12.4964, "IT", LocalNames("Rome", "Roma"))))

            // Act
            viewModel.fetchCitySuggestions("Rome", isDeparture = true)
            advanceUntilIdle()

            // Assert
            val state = viewModel.departureCityLoadingState.value
            assertThat(
                state,
            ).isEqualTo(TravelViewModel.LoadingState.Loaded(listOf(City("Rome", 41.9028, 12.4964, "IT", LocalNames("Rome", "Roma")))))
            coVerify(exactly = 2) { getCitySuggestionsUseCase.invoke("Rome") }
        }

    @Test
    fun `loadLastTravelInfo should handle no data found gracefully`() =
        runTest {
            // Arrange
            coEvery { getLastTravelInfoUseCase() } returns null

            // Act
            viewModel.loadLastTravelInfo()
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.travelInfo.value).isNull()
        }

    @Test
    fun `monitorLoadingStates should trigger callback once loading completes`() =
        runTest {
            // Arrange
            val callbackTriggered = mutableListOf<Boolean>()
            val onTravelInfoSaved: () -> Unit = { callbackTriggered.add(true) }

            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns true

            coEvery { saveTravelInfoUseCase.invoke(travelEntity) } just Runs

            every { weatherViewModel.fetchWeatherData(any(), any(), any(), any()) } answers {
                viewModel._isWeatherLoading.value = false
            }
            every { chatGptViewModel.getLocalInfoForDestination(any()) } answers {
                viewModel._isLocalInfoLoading.value = false
            }

            every { chatGptViewModel.checklistItems } returns MutableStateFlow(emptyList())

            // Act
            viewModel.saveTravelInfo(travelEntity, chatGptViewModel, weatherViewModel, onTravelInfoSaved)

            advanceUntilIdle()

            // Assert
            assertThat(callbackTriggered).hasSize(1)
            assertThat(viewModel.isLoading.value).isFalse()
        }

    @Test
    fun `fetchCitySuggestions should handle multiple API failures gracefully`() =
        runTest {
            // Arrange
            val errorMessage = "API failure"
            coEvery { getCitySuggestionsUseCase(any()) } throws Exception(errorMessage)

            // Act
            viewModel.fetchCitySuggestions("NonExistentCity", isDeparture = true)
            advanceUntilIdle()

            // Assert
            val state = viewModel.departureCityLoadingState.value
            assertThat(state).isInstanceOf(TravelViewModel.LoadingState.Error::class.java)
            assertThat((state as TravelViewModel.LoadingState.Error).message).isEqualTo("Failed to load suggestions")
        }

    @Test
    fun `saveTravelInfo should handle simultaneous save and delete operations without conflicts`() =
        runTest {
            // Arrange
            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns true

            coEvery { saveTravelInfoUseCase(any()) } just Runs
            coEvery { database.clearAllTables() } just Runs

            // Act
            viewModel.saveTravelInfo(travelEntity, chatGptViewModel, weatherViewModel) {}
            viewModel.deleteTravelInfo {}
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.travelInfo.value).isNull()
            coVerify { database.clearAllTables() }
            coVerify(exactly = 1) { saveTravelInfoUseCase.invoke(eq(travelEntity)) }
        }

    @Test
    fun `fetchCitySuggestions should handle slow network gracefully`() =
        runTest {
            // Arrange
            val slowFlow =
                flow {
                    delay(500) // Simulate slow network with delay
                    emit(listOf(City("Madrid", 40.4168, -3.7038, "ES", LocalNames("Madrid", "Madrid"))))
                }
            coEvery { getCitySuggestionsUseCase(any()) } returns slowFlow

            // Act
            viewModel.fetchCitySuggestions("Madrid", isDeparture = true)
            advanceTimeBy(500) // Simulate network delay
            advanceUntilIdle()

            // Assert
            val state = viewModel.departureCityLoadingState.value
            assertThat(
                state,
            ).isEqualTo(TravelViewModel.LoadingState.Loaded(listOf(City("Madrid", 40.4168, -3.7038, "ES", LocalNames("Madrid", "Madrid")))))
        }

    @Test
    fun `fetchCitySuggestions should handle special characters in query`() =
        runTest {
            // Arrange
            val specialCharCityList = listOf(City("New York", 40.7128, -74.0060, "US", LocalNames("New York", "New York")))
            coEvery { getCitySuggestionsUseCase("@New!") } returns flowOf(specialCharCityList)

            // Act
            viewModel.fetchCitySuggestions("@New!", isDeparture = true)
            advanceUntilIdle()

            // Assert
            coVerify { getCitySuggestionsUseCase.invoke("@New!") }
            assertThat(viewModel.departureCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded(specialCharCityList))
        }

    @Test
    fun `fetchCitySuggestions should handle rapid calls with cancellation`() =
        runTest {
            // Arrange
            val cityList1 = listOf(City("Paris", 48.8566, 2.3522, "FR", LocalNames("Paris", "Paris")))
            val cityList2 = listOf(City("Berlin", 52.52, 13.405, "DE", LocalNames("Berlin", "Berlin")))
            coEvery { getCitySuggestionsUseCase("Par") } returns flowOf(cityList1)
            coEvery { getCitySuggestionsUseCase("Ber") } returns flowOf(cityList2)

            // Act
            viewModel.fetchCitySuggestions("Par", isDeparture = true)
            viewModel.fetchCitySuggestions("Ber", isDeparture = true)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { getCitySuggestionsUseCase.invoke("Ber") }
            coVerify(exactly = 0) { getCitySuggestionsUseCase.invoke("Par") }
        }

    @Test
    fun `fetchCitySuggestions should handle concurrent requests with different queries`() =
        runTest {
            // Arrange
            val cityList1 = listOf(City("London", 51.5074, -0.1278, "UK", LocalNames("London", "Londres")))
            val cityList2 = listOf(City("Tokyo", 35.6762, 139.6503, "JP", LocalNames("Tokyo", "東京")))
            coEvery { getCitySuggestionsUseCase("Lon") } returns flowOf(cityList1)
            coEvery { getCitySuggestionsUseCase("Tok") } returns flowOf(cityList2)

            // Act
            viewModel.fetchCitySuggestions("Lon", isDeparture = true)
            viewModel.fetchCitySuggestions("Tok", isDeparture = false)
            advanceUntilIdle()

            // Assert
            coVerify { getCitySuggestionsUseCase.invoke("Lon") }
            coVerify { getCitySuggestionsUseCase.invoke("Tok") }
            assertThat(viewModel.departureCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded(cityList1))
            assertThat(viewModel.arrivalCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded(cityList2))
        }

    @Test
    fun `fetchCitySuggestions should handle multiple retries on consecutive errors`() =
        runTest {
            // Arrange
            val consecutiveError = Exception("Consecutive network errors")
            coEvery { getCitySuggestionsUseCase(any()) } throws consecutiveError andThenThrows consecutiveError andThen
                flowOf(listOf(City("Rome", 41.9028, 12.4964, "IT", LocalNames("Rome", "Roma"))))

            // Act
            viewModel.fetchCitySuggestions("Rome", isDeparture = true)
            advanceUntilIdle()

            // Assert
            val state = viewModel.departureCityLoadingState.value
            assertThat(state).isEqualTo(
                TravelViewModel.LoadingState.Loaded(
                    listOf(City("Rome", 41.9028, 12.4964, "IT", LocalNames("Rome", "Roma"))),
                ),
            )
            coVerify(exactly = 3) { getCitySuggestionsUseCase.invoke("Rome") }
        }

    @Test
    fun `fetchCitySuggestions should set Loaded state on successful fetch`() =
        runTest {
            // Arrange
            val cityList = listOf(City("Berlin", 52.52, 13.405, "DE", LocalNames("Berlin", "Berlin")))
            coEvery { getCitySuggestionsUseCase("Berlin") } returns flowOf(cityList)

            // Act
            viewModel.fetchCitySuggestions("Berlin", isDeparture = true)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.departureCityLoadingState.value).isEqualTo(TravelViewModel.LoadingState.Loaded(cityList))
            coVerify { getCitySuggestionsUseCase.invoke("Berlin") }
        }

    @Test
    fun `initiateChecklistGeneration should trigger checklist generation with correct parameters`() =
        runTest {
            // Arrange
            val mockChatGptViewModel = mockk<ChatGptViewModel>(relaxed = true)
            val travelEntity =
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
            val weatherData =
                listOf(
                    WeatherEntity("1", LocalDate.parse("2024-10-10"), 25.0, 18.0, "Sunny"), // Corrected types
                    WeatherEntity("2", LocalDate.parse("2024-10-11"), 22.0, 16.0, "Cloudy"), // Corrected types
                )

            // Act
            viewModel.initiateChecklistGeneration(mockChatGptViewModel, travelEntity, weatherData)

            // Assert
            assertThat(viewModel._isChecklistLoading.value).isTrue()
            coVerify(exactly = 1) {
                mockChatGptViewModel.generateChecklist(
                    departureLocation = travelEntity.departurePlace,
                    departureDate = travelEntity.departureDate,
                    destination = travelEntity.arrivalPlace,
                    arrivalDate = travelEntity.arrivalDate,
                    travelMethod = travelEntity.travelMethod,
                    weatherData = weatherData,
                    daysToStay = travelEntity.daysToStay,
                )
            }
        }

    @Test
    fun `saveTravelInfo should proceed when network is available`() =
        runTest {
            // Arrange
            mockkStatic("com.erendogan6.seyahatasistanim.utils.NetworkUtilsKt")
            every { isNetworkAvailable(context) } returns true
            coEvery { saveTravelInfoUseCase.invoke(travelEntity) } just Runs

            // Act
            viewModel.saveTravelInfo(travelEntity, chatGptViewModel, weatherViewModel) {}

            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { saveTravelInfoUseCase.invoke(eq(travelEntity)) }
            assertThat(viewModel.travelInfo.value).isNotNull
            assertThat(viewModel.travelInfo.value).isEqualTo(travelEntity)

            assertThat(viewModel._isTravelInfoLoading.value).isFalse()
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
}
