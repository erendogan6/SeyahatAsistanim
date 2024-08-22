package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.ui.viewmodel.ChatGptViewModel
import com.erendogan6.seyahatasistanim.ui.viewmodel.TravelViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.koin.androidx.compose.koinViewModel

@Composable
fun localInfoScreen(
    modifier: Modifier = Modifier,
    travelViewModel: TravelViewModel = koinViewModel(),
    chatGptViewModel: ChatGptViewModel = koinViewModel(),
) {
    val travelInfo by travelViewModel.travelInfo.collectAsState()
    val localInfo by chatGptViewModel.localInfo.collectAsState()
    val isLoading by chatGptViewModel.isLoading.collectAsState()
    val error by chatGptViewModel.error.collectAsState()

    LaunchedEffect(travelInfo) {
        travelInfo?.arrivalPlace?.let { destination ->
            chatGptViewModel.getLocalInfoForDestination(destination)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.local_info),
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
        )

        when {
            isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            error != null -> {
                Text(
                    text = stringResource(id = R.string.error, error.toString()),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.error,
                        ),
                    textAlign = TextAlign.Center,
                )
            }
            localInfo != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 4.dp,
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors =
                                                listOf(
                                                    MaterialTheme.colorScheme.surface,
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                ),
                                        ),
                                    ),
                        ) {
                            MarkdownText(
                                markdown = localInfo!!.info,
                                modifier = Modifier.padding(16.dp),
                                style =
                                    TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        textAlign = TextAlign.Start,
                                    ),
                            )
                        }
                    }
                }
            }
            else -> {
                Text(
                    text = stringResource(id = R.string.no_local_info_found),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
