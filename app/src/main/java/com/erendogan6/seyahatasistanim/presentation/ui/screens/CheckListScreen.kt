package com.erendogan6.seyahatasistanim.presentation.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity
import com.erendogan6.seyahatasistanim.presentation.viewmodel.ChatGptViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun checklistScreen(
    modifier: Modifier = Modifier,
    chatGptViewModel: ChatGptViewModel = koinViewModel(),
) {
    val checklistItems by chatGptViewModel.checklistItems.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var newItem by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        chatGptViewModel.loadChecklistItems()
    }

    val sortedChecklistItems = checklistItems.sortedByDescending { it.isCompleted }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.background,
                                ),
                        ),
                ).padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.checklist_title),
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = Color.Gray.copy(alpha = 0.1f),
                    ),
            ) {
                Text(
                    text = stringResource(id = R.string.checklist_info),
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    modifier = Modifier.padding(10.dp),
                )
            }

            if (sortedChecklistItems.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = stringResource(id = R.string.no_checklist_items),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium,
                            ),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sortedChecklistItems) { item ->
                        checklistItemCard(
                            item = item,
                            onDeleteClick = { chatGptViewModel.deleteChecklistItem(item.id) },
                            onCompleteClick = {
                                val wasCompleted = item.isCompleted
                                chatGptViewModel.toggleItemCompletion(item.id)

                                if (!wasCompleted && !chatGptViewModel.notificationShown) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Congratulations on completing a task!")
                                        chatGptViewModel.setNotificationShown()
                                    }
                                }
                            },
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    modifier =
                        Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(50))
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(50),
                            ).padding(horizontal = 16.dp),
                    placeholder = { Text(text = stringResource(id = R.string.add_new_item)) },
                    colors =
                        TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                )
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = {
                        if (newItem.isNotEmpty()) {
                            chatGptViewModel.addChecklistItem(newItem)
                            newItem = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .shadow(10.dp, RoundedCornerShape(50))
                            .size(56.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(60.dp)),
        )
    }
}

@Composable
fun checklistItemCard(
    item: ChecklistItemEntity,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    val cardElevation by animateFloatAsState(targetValue = if (item.isCompleted) 2.dp.value else 6.dp.value)

    val backgroundColor =
        if (item.isCompleted) {
            Color(0xFFDFF8E1)
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }

    val textColor =
        if (item.isCompleted) {
            Color(0xFF4CAF50)
        } else {
            Color(0xFF333333)
        }

    val iconColor =
        if (item.isCompleted) {
            Color(0xFF4CAF50)
        } else {
            Color(0xFF3F51B5)
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(cardElevation.dp, RoundedCornerShape(16.dp))
                .background(color = backgroundColor)
                .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(cardElevation.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.item,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onCompleteClick) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = stringResource(id = R.string.complete),
                    tint = iconColor,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
