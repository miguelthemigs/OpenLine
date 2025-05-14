package com.example.openline.ui.screens

import UserName
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    // 1) split root‐level vs replies
    val topComments = remember(allComments) {
        allComments.filter { it.parentCommentId == null }
    }
    val repliesMap = remember(allComments) {
        allComments.groupBy { it.parentCommentId }
    }

    var selectedTab by remember { mutableStateOf("Top") }
    val displayedComments = remember(selectedTab, topComments) {
        if (selectedTab == "Top") topComments.sortedByDescending { it.likes }
        else topComments.sortedByDescending { it.timestamp }
    }

    // track which comment is expanded
    var expandedId by remember { mutableStateOf<UUID?>(null) }

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
            // — Opinion Header Card —
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = opinion.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 24.sp
                        ),
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
                                Text("AGREE", fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${opinion.likes}", color = TextSecondary)
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
                                Text("DISAGREE", fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${opinion.dislikes}", color = TextSecondary)
                        }
                    }
                }
            }

            Divider(color = DividerColor, thickness = 1.dp)

            // — Tabs —
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                listOf("Top","Newest").forEach { tab ->
                    val sel = tab == selectedTab
                    TextButton(
                        onClick = { selectedTab = tab },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 32.dp),
                        // reduce vertical padding so the button is less tall
                        contentPadding = PaddingValues(
                            vertical = 4.dp,
                            horizontal = 12.dp
                        ),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if(sel) ColorPrimary else CardBackground,
                            contentColor   = if(sel) ColorOnPrimary else TextSecondary
                        )
                    ) {
                        Text(tab)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }

            // — Comments + Collapsible Replies —
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(displayedComments, key={it.id}) { comment ->
                    val replies    = repliesMap[comment.id].orEmpty()
                    val isExpanded = comment.id == expandedId
                    val replyCount = replies.size

                    // Top‐level comment row (tappable)
                    CommentItem(
                        comment       = comment,
                        repliesCount  = replyCount,
                        onRepliesClick= { expandedId = if(isExpanded) null else comment.id },
                        onReact       = { like ->
                            onReactComment(comment.id.toString(), like)
                        },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .clickable { onCommentClick(comment) }
                            .padding(vertical = 8.dp)
                    )

                    // Inline previews
                    if(isExpanded && replies.isNotEmpty()){
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(start=32.dp, end=16.dp, bottom=16.dp)
                        ) {
                            replies.take(2).forEach { reply ->
                                CommentItem(
                                    comment       = reply,
                                    repliesCount  = 0,
                                    onRepliesClick= null,
                                    onReact       = { l -> onReactComment(reply.id.toString(),l) },
                                    modifier      = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                            if(replyCount>2){
                                Text(
                                    "See more…",
                                    style    = MaterialTheme.typography.labelMedium,
                                    color    = ColorPrimary,
                                    modifier = Modifier
                                        .clickable { onCommentClick(comment) }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
