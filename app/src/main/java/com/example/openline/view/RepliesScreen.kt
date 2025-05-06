package com.example.openline.view

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
    onReactComment: (commentId: String, like: Boolean) -> Unit
) {
    // Debug: log the incoming list once
    LaunchedEffect(allComments) {
        Log.d("RepliesScreen", "ALL COMMENTS:")
        allComments.forEach {
            Log.d("RepliesScreen", "→ id=${it.id}, parent=${it.parentCommentId}")
        }
    }

    val replies = allComments.filter { it.parentCommentId == parent.id }
    LaunchedEffect(replies) {
        Log.d("RepliesScreen", "Filtered ${replies.size} replies for parent ${parent.id}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CommentItem(
                comment = parent,
                onReact = { onReactComment(parent.id.toString(), it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Divider()
            if (replies.isEmpty()) {
                Text(
                    "No replies yet.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    items(replies, key = { it.id }) { reply ->
                        CommentItem(
                            comment = reply,
                            onReact = { onReactComment(reply.id.toString(), it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
