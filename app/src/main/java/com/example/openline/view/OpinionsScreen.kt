package com.example.openline.ui.screens

import UserName
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OpinionScreen(
    opinion: Opinion,
    allComments: List<Comment>,
    author: String,
    onBack: () -> Unit,
    onReactOpinion: (String, Boolean) -> Unit,
    onReactComment: (String, Boolean) -> Unit,
    onCommentClick: (Comment) -> Unit,
    onSubmitReply: (String, String) -> Unit
) {
    // split top-level vs replies
    val topComments = remember(allComments) {
        allComments.filter { it.parentCommentId == null }
    }
    val repliesMap = remember(allComments) {
        allComments.groupBy { it.parentCommentId }
    }

    // tab state + sorted list
    var selectedTab by remember { mutableStateOf("Top") }
    val displayedComments = remember(selectedTab, topComments) {
        if (selectedTab == "Top")
            topComments.sortedByDescending { it.likes }
        else
            topComments.sortedByDescending { it.timestamp }
    }

    // inline-replies expansion
    var expandedId by remember { mutableStateOf<UUID?>(null) }

    // reply-to-opinion composer
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    // scroll state so we always land on the first comment when switching tabs
    val listState = rememberLazyListState()
    LaunchedEffect(selectedTab) {
        listState.scrollToItem(0)
    }

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
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Opinion header
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        opinion.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 24.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "$author • ${opinion.timestamp.toLocalTime()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Agree & Disagree buttons + counts
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { onReactOpinion(opinion.id.toString(), true) },
                                border = BorderStroke(1.dp, AgreeGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AgreeGreen),
                                modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.ThumbUp, contentDescription = "Agree", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("AGREE", fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(4.dp))
                            Text("${opinion.likes}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { onReactOpinion(opinion.id.toString(), false) },
                                border = BorderStroke(1.dp, DisagreeRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DisagreeRed),
                                modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.ThumbDown, contentDescription = "Disagree", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("DISAGREE", fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(4.dp))
                            Text("${opinion.dislikes}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // Reply to opinion
                    Button(
                        onClick = { showReplyField = !showReplyField },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Text("Reply", color = ColorOnPrimary, fontSize = 14.sp)
                    }
                    if (showReplyField) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = {
                                onSubmitReply(opinion.id.toString(), replyText)
                                replyText = ""
                                showReplyField = false
                            },
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Post", fontSize = 14.sp)
                        }
                    }
                }
            }

            Divider(color = DividerColor, thickness = 1.dp)

            // Tabs
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                listOf("Top", "Newest").forEach { tab ->
                    val sel = tab == selectedTab
                    TextButton(
                        onClick = { selectedTab = tab },
                        modifier = Modifier.defaultMinSize(minHeight = 28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (sel) ColorPrimary else CardBackground,
                            contentColor = if (sel) ColorOnPrimary else TextSecondary
                        )
                    ) {
                        Text(tab, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }

            // Comments with inline replies
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                items(displayedComments, key = { it.id }) { comment ->
                    val replies = repliesMap[comment.id].orEmpty()
                    val isExpanded = comment.id == expandedId
                    val replyCount = replies.size

                    CommentItem(
                        comment = comment,
                        repliesCount = replyCount,
                        onRepliesClick = { expandedId = if (isExpanded) null else comment.id },
                        onReact = { like -> onReactComment(comment.id.toString(), like) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCommentClick(comment) }
                            .padding(vertical = 6.dp)
                    )

                    if (isExpanded && replies.isNotEmpty()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 28.dp, end = 12.dp, bottom = 12.dp)
                        ) {
                            replies.take(2).forEach { reply ->
                                CommentItem(
                                    comment = reply,
                                    repliesCount = 0,
                                    onRepliesClick = null,
                                    onReact = { l -> onReactComment(reply.id.toString(), l) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                )
                            }
                            if (replyCount > 2) {
                                Text(
                                    "See more…",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ColorPrimary,
                                    modifier = Modifier
                                        .clickable { onCommentClick(comment) }
                                        .padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
