package com.example.openline.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.*
import com.example.openline.utils.timeAgo
import com.example.openline.model.Comment
import com.example.openline.model.Opinion
import com.example.openline.viewmodel.UsersViewModel

import androidx.lifecycle.viewmodel.compose.viewModel


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OpinionScreen(
    opinion: Opinion,
    comments: List<Comment>,
    author: String,
    onBack: () -> Unit,
    onReactOpinion: (opinionId: String, like: Boolean) -> Unit,
    onReactComment: (commentId: String, like: Boolean) -> Unit,
    onReply: (opinionId: String) -> Unit,
    onSubmitReply: (opinionId: String, text: String) -> Unit // 🔹 Added callback
) {
    var selectedTab by remember { mutableStateOf("Top") }
    val displayedComments = remember(selectedTab, comments) {
        if (selectedTab == "Top") comments.sortedByDescending { it.likes }
        else comments.sortedByDescending { it.timestamp }
    }

    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opinion for “Item”") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = ColorOnPrimary,
                    navigationIconContentColor = ColorOnPrimary
                )
            )
        },
        containerColor = CardBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // — Opinion Card —
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = opinion.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$author • ${opinion.timestamp.toLocalTime()}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { onReactOpinion(opinion.id.toString(), true) },
                            border = BorderStroke(1.dp, AgreeGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AgreeGreen)
                        ) {
                            Icon(Icons.Outlined.ThumbUp, contentDescription = "Agree")
                            Spacer(Modifier.width(4.dp))
                            Text("AGREE")
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("${opinion.likes}", color = TextPrimary)

                        Spacer(Modifier.width(24.dp))

                        OutlinedButton(
                            onClick = { onReactOpinion(opinion.id.toString(), false) },
                            border = BorderStroke(1.dp, DisagreeRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DisagreeRed)
                        ) {
                            Icon(Icons.Outlined.ThumbDown, contentDescription = "Disagree")
                            Spacer(Modifier.width(4.dp))
                            Text("DISAGREE")
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("${opinion.dislikes}", color = TextPrimary)
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(onClick = { showReplyField = true }) {
                        Text("Reply")
                    }

                    if (showReplyField) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            label = { Text("Write a reply...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onSubmitReply(opinion.id.toString(), replyText)
                                replyText = ""
                                showReplyField = false
                            }
                        ) {
                            Text("Post Reply")
                        }
                    }
                }
            }

            Divider(color = DividerColor, thickness = 1.dp)

            // — Tabs —
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                listOf("Top", "Newest").forEach { tab ->
                    val isSelected = tab == selectedTab
                    TextButton(
                        onClick = { selectedTab = tab },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (isSelected) ColorPrimary else CardBackground,
                            contentColor = if (isSelected) ColorOnPrimary else TextSecondary
                        )
                    ) {
                        Text(tab)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }

            // — Comments List —
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(displayedComments) { comment ->
                    CommentItem(
                        comment = comment,
                        onReact = { like -> onReactComment(comment.id.toString(), like) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    comment: Comment,
    onReact: (like: Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CoPresent,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                UserName(
                    userId = comment.userId.toString(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = timeAgo(comment.timestamp),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(comment.text, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = TextSecondary,
                    modifier = Modifier
                        .clickable { onReact(true) }
                        .size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(comment.likes.toString(), color = TextSecondary)

                Spacer(Modifier.width(16.dp))

                Icon(
                    Icons.Outlined.ThumbDown,
                    contentDescription = "Dislike",
                    tint = TextSecondary,
                    modifier = Modifier
                        .clickable { onReact(false) }
                        .size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(comment.dislikes.toString(), color = TextSecondary)
            }
        }
    }
}

@Composable
fun UserName(userId: String, modifier: Modifier = Modifier) {
    val viewModel: UsersViewModel = viewModel()
    var name by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        name = viewModel.fetchUserName(userId) ?: "Unknown"
    }

    Text(text = name ?: "Loading…", modifier = modifier)
}

