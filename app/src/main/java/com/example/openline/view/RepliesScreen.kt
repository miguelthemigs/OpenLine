package com.example.openline.view

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.openline.model.Comment
import com.example.openline.ui.screens.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RepliesScreen(
    parent: Comment,
    allComments: List<Comment>,
    onBack: () -> Unit,
    onReactComment: (commentId: String, like: Boolean) -> Unit,
    onSubmitReply: (parentCommentId: String, text: String) -> Unit
) {
    // debug log incoming list
    LaunchedEffect(allComments) {
        Log.d("RepliesScreen", "ALL COMMENTS:")
        allComments.forEach {
            Log.d("RepliesScreen", "→ id=${it.id}, parent=${it.parentCommentId}")
        }
    }

    val replies = allComments.filter { it.parentCommentId == parent.id }
    LaunchedEffect(replies) {
        Log.d("RepliesScreen", "Found ${replies.size} replies for parent ${parent.id}")
    }

    // reply input state
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    // animated list state + haptics
    val listState = rememberLazyListState()
    val haptic    = LocalHapticFeedback.current
    var lastTopIndex by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Parent comment
            CommentItem(
                comment = parent,
                onReact = { onReactComment(parent.id.toString(), it) },
                repliesCount = replies.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Reply composer
            Spacer(Modifier.height(4.dp))
            if (!showReplyField) {
                Button(
                    onClick = { showReplyField = true },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) { Text("Reply") }
            } else {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Write a reply…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        onSubmitReply(parent.id.toString(), replyText)
                        replyText = ""
                        showReplyField = false
                    }) {
                        Text("Post")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()

            // Animated replies list
            if (replies.isEmpty()) {
                Text(
                    "No replies yet.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    itemsIndexed(replies, key = { _, it -> it.id }) { idx, reply ->
                        // find this reply's position in viewport
                        val info     = listState.layoutInfo
                        val itemInfo = info.visibleItemsInfo.firstOrNull { it.index == idx }
                        val vh        = info.viewportSize.height.toFloat()
                        val offsetY   = itemInfo?.offset?.toFloat() ?: 0f
                        val normPos   = (1f - (offsetY / vh)).coerceIn(0f, 1f)

                        // scale [0.7..1.0], alpha [0.5..1.0]
                        val targetScale by remember(normPos) { derivedStateOf { 0.7f + normPos * 0.3f } }
                        val targetAlpha by remember(normPos) { derivedStateOf { 0.5f + normPos * 0.5f } }
                        val scale    by animateFloatAsState(targetScale)
                        val alpha    by animateFloatAsState(targetAlpha)

                        // alternating tilt: even→+10°, odd→−10°
                        val maxTilt = 10f
                        val direction = if (idx % 2 == 0) 1 else -1
                        val targetRotation by remember(normPos) {
                            derivedStateOf { (1f - normPos) * maxTilt * direction }
                        }
                        val rotation by animateFloatAsState(targetRotation)

                        // light haptic when a reply hits top
                        LaunchedEffect(itemInfo?.offset) {
                            if (itemInfo?.offset == 0 && lastTopIndex != idx) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastTopIndex = idx
                            }
                        }

                        // render animated CommentItem
                        CommentItem(
                            comment = reply,
                            onReact = { onReactComment(reply.id.toString(), it) },
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    rotationZ = rotation
                                }
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
