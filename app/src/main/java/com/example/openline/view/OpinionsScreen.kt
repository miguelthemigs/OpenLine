package com.example.openline.ui.screens

import UserName
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.*
import com.example.openline.model.Comment
import com.example.openline.model.Opinion
import com.example.openline.utils.timeAgo

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
    onCommentClick: (Comment) -> Unit,
    onSubmitReply: (opinionId: String, text: String) -> Unit
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
                            imageVector = Icons.Outlined.CoPresent,
                            contentDescription = "User avatar",
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
                    // — Reaction buttons —
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Agree
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinedButton(
                                onClick = { onReactOpinion(opinion.id.toString(), true) },
                                border = BorderStroke(1.dp, AgreeGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AgreeGreen),
                                modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                            ) {
                                Icon(Icons.Outlined.ThumbUp, contentDescription = "Agree")
                                Spacer(Modifier.width(4.dp))
                                Text("AGREE", style = MaterialTheme.typography.labelLarge, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${opinion.likes}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        // Disagree
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinedButton(
                                onClick = { onReactOpinion(opinion.id.toString(), false) },
                                border = BorderStroke(1.dp, DisagreeRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DisagreeRed),
                                modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                            ) {
                                Icon(Icons.Outlined.ThumbDown, contentDescription = "Disagree")
                                Spacer(Modifier.width(4.dp))
                                Text("DISAGREE", style = MaterialTheme.typography.labelLarge, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${opinion.dislikes}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { showReplyField = true }) { Text("Reply") }
                    if (showReplyField) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            label = { Text("Write a reply...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            onSubmitReply(opinion.id.toString(), replyText)
                            replyText = ""
                            showReplyField = false
                        }) {
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

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(displayedComments) { comment ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCommentClick(comment) }
                            .padding(vertical = 4.dp)
                    ) {
                        CommentItem(
                            comment = comment,
                            onReact = { like -> onReactComment(comment.id.toString(), like) }
                        )
                    }
                }
            }
        }
    }
}
