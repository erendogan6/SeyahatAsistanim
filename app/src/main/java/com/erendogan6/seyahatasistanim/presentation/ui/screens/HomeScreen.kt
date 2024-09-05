package com.erendogan6.seyahatasistanim.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.presentation.viewmodel.TravelViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun homeScreen(
    onNavigateToWeather: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToLocalInfo: () -> Unit,
    onNavigateToChatGpt: () -> Unit,
    onNavigateToTravelInfo: () -> Unit,
    modifier: Modifier = Modifier,
    travelViewModel: TravelViewModel = koinViewModel(),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = stringResource(id = R.string.app_logo),
            modifier =
                Modifier
                    .size(250.dp)
                    .padding(top = 48.dp, bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .testTag("homeScreenAppLogo"),
        )

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.primary,
        )

        buttonWithIcon(
            text = stringResource(id = R.string.weather_details),
            iconResId = R.drawable.ic_cloud,
            onClick = onNavigateToWeather,
        )

        Spacer(modifier = Modifier.height(16.dp))

        buttonWithIcon(
            text = stringResource(id = R.string.checklist),
            iconResId = R.drawable.ic_checklist,
            onClick = onNavigateToChecklist,
        )

        Spacer(modifier = Modifier.height(16.dp))

        buttonWithIcon(
            text = stringResource(id = R.string.local_info),
            iconResId = R.drawable.ic_info,
            onClick = onNavigateToLocalInfo,
        )

        Spacer(modifier = Modifier.height(16.dp))

        buttonWithIcon(
            text = stringResource(id = R.string.chat_gpt_interaction),
            iconResId = R.drawable.ic_chat,
            onClick = onNavigateToChatGpt,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                travelViewModel.deleteTravelInfo {
                    onNavigateToTravelInfo()
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            shape = MaterialTheme.shapes.medium,
            elevation = ButtonDefaults.buttonElevation(8.dp),
        ) {
            Text(text = stringResource(id = R.string.new_travel), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun buttonWithIcon(
    text: String,
    iconResId: Int? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(8.dp),
    ) {
        iconResId?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
