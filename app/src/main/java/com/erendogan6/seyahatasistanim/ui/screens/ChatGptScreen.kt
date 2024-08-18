package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erendogan6.seyahatasistanim.ui.viewmodel.ChatGptViewModel

@Composable
fun chatGptScreen(modifier: Modifier = Modifier) {
    val viewModel: ChatGptViewModel = viewModel()
    var userInput by remember { mutableStateOf("") }
    val chatGptResponse by viewModel.chatGptResponse.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "ChatGPT ile Etkileşim",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        chatGptResponse?.let { response ->
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(response.choices) { choice ->
                    Text(
                        text = choice.message.content,
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } ?: run {
            Text(text = "Yanıt bekleniyor...", modifier = Modifier.padding(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Mesajınızı yazın") },
            )
            Button(onClick = {
                if (userInput.isNotEmpty()) {
                    viewModel.getSuggestions(prompt = userInput)
                    userInput = ""
                }
            }) {
                Text(text = "Gönder")
            }
        }
    }
}
