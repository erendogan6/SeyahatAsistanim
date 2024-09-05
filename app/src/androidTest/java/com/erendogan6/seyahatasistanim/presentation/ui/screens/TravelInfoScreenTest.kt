package com.erendogan6.seyahatasistanim.presentation.ui.screens

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.di.databaseModule
import com.erendogan6.seyahatasistanim.di.networkModule
import com.erendogan6.seyahatasistanim.di.repositoryModule
import com.erendogan6.seyahatasistanim.di.useCaseModule
import com.erendogan6.seyahatasistanim.di.viewModelModule
import com.erendogan6.seyahatasistanim.presentation.ui.components.appNavigation
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TravelInfoScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
        startKoinIfNeeded()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testTravelInfoScreen_displaysAndFillsAllFieldsCorrectly() {
        // Test ekranını hazırla
        composeTestRule.setContent {
            val navController = rememberNavController()
            appNavigation(
                navController = navController,
                modifier = Modifier,
                startDestination = "travelInfo",
            )
        }

        verifyAllFieldsDisplayed()

        selectAndVerifyDate(R.string.departure_date, 3)
        selectAndVerifyDate(R.string.arrival_date, 5)

        fillAndVerifyTextInput(R.string.departure_place, "istanbul", "İstanbul - TR")
        fillAndVerifyTextInput(R.string.arrival_place, "ankara", "Ankara - TR")

        selectAndVerifyTravelMethod(R.string.plane)

        fillAndVerifyTextByTag("DaysToStayTextField", "3")

        composeTestRule.onNodeWithText(context.getString(R.string.continue_text)).performClick()

        verifyLoadingAnimationIsDisplayed()

        verifyNavigationToHomeScreen()
    }

    private fun verifyLoadingAnimationIsDisplayed() {
        composeTestRule.onNodeWithTag("lottieLoadingScreen").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun verifyNavigationToHomeScreen() {
        composeTestRule.waitUntilAtLeastOneExists(
            hasText(context.getString(R.string.app_name)),
            timeoutMillis = 15000,
        )
    }

    private fun startKoinIfNeeded() {
        if (org.koin.core.context.GlobalContext
                .getOrNull() == null
        ) {
            startKoin {
                androidContext(context)
                modules(
                    listOf(
                        networkModule,
                        viewModelModule,
                        databaseModule,
                        repositoryModule,
                        useCaseModule,
                    ),
                )
            }
        }
    }

    private fun verifyAllFieldsDisplayed() {
        val fieldsToCheck =
            listOf(
                R.string.welcome_message,
                R.string.enter_travel_info,
                R.string.departure_date,
                R.string.arrival_date,
                R.string.departure_place,
                R.string.arrival_place,
                R.string.travel_method,
                R.string.days_to_stay,
                R.string.continue_text,
            )

        fieldsToCheck.forEach { resId ->
            composeTestRule.onNodeWithText(context.getString(resId)).assertIsDisplayed()
        }
    }

    private fun selectAndVerifyDate(
        dateFieldResId: Int,
        daysToAdd: Long,
    ) {
        val dateText = getFormattedDate(daysToAdd)
        composeTestRule.onNodeWithText(context.getString(dateFieldResId)).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasText(dateText, substring = true) and hasClickAction()).onFirst().performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.ok)).performClick()
    }

    private fun getFormattedDate(daysToAdd: Long): String =
        LocalDate.now().plusDays(daysToAdd).format(DateTimeFormatter.ofPattern("d", Locale.getDefault()))

    @OptIn(ExperimentalTestApi::class)
    private fun fillAndVerifyTextInput(
        fieldResId: Int,
        inputText: String,
        expectedText: String,
    ) {
        val fieldNode = composeTestRule.onNodeWithText(context.getString(fieldResId))
        fieldNode.performClick()
        inputText.forEach { char ->
            fieldNode.performTextInput(char.toString())
            composeTestRule.waitForIdle()
        }
        composeTestRule.waitUntilAtLeastOneExists(
            hasText(expectedText) and hasClickAction(),
            timeoutMillis = 5000,
        )
        composeTestRule
            .onAllNodes(hasText(expectedText))
            .onFirst()
            .assertIsDisplayed()
            .performClick()
    }

    private fun fillAndVerifyTextByTag(
        tag: String,
        inputText: String,
    ) {
        val textFieldNode = composeTestRule.onNodeWithTag(tag)
        textFieldNode.assertIsDisplayed().performClick()
        textFieldNode.performTextInput(inputText)
        composeTestRule.onNodeWithText(inputText, ignoreCase = true).assertExists()
    }

    private fun selectAndVerifyTravelMethod(methodResId: Int) {
        composeTestRule.onNodeWithText(context.getString(R.string.travel_method)).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(context.getString(methodResId)).performClick()
        composeTestRule.waitForIdle()
    }
}
