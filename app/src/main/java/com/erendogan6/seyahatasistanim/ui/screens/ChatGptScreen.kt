package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.erendogan6.seyahatasistanim.ui.viewmodel.ChatGptViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun chatGptScreen(
    modifier: Modifier = Modifier,
    chatGptViewModel: ChatGptViewModel = koinViewModel(),
) {
    var userInput by remember { mutableStateOf("") }
    val chatGptResponse by chatGptViewModel.chatGptResponse.collectAsState()
    val error by chatGptViewModel.error.collectAsState()
    val isLoading by chatGptViewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5)),
                        ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            // Başlık
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ChatGPT ile Etkileşim",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Mesaj Listesi
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                chatGptResponse?.let { response ->
                    items(response.choices) { choice ->
                        messageBubble(
                            message = choice.message.content,
                            isUserMessage = false,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Hata veya Yükleniyor göstergesi
            when {
                isLoading -> loadingAnimation()
                error != null -> errorMessage(error = error!!)
            }

            // Mesaj Giriş Alanı
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier =
                        Modifier
                            .weight(1f)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(24.dp),
                            ),
                    placeholder = { Text("Mesajınızı yazın") },
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            chatGptViewModel.getLocalInfoForDestination(destination = userInput)
                            userInput = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
fun messageBubble(
    message: String,
    isUserMessage: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            color =
                if (isUserMessage) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                },
            modifier =
                Modifier
                    .align(if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart)
                    .widthIn(max = 300.dp),
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun loadingAnimation() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
fun errorMessage(error: String) {
    Text(
        text = "Hata: $error",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp),
    )
}
