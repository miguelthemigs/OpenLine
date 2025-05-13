package com.example.openline

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openline.model.Comment
import com.example.openline.model.Opinion
import com.example.openline.ui.screens.OpinionScreen
import com.example.openline.ui.theme.OpenLineTheme
import com.example.openline.view.RepliesScreen
import com.example.openline.viewmodel.CommentsViewModel
import com.example.openline.viewmodel.OpinionsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OpenLineTheme {
                val opinionId  = "9c30f864-9499-4d57-9a2b-fd2c2d427532"
                val vm         : OpinionsViewModel = viewModel()
                val commentsVm : CommentsViewModel = viewModel()
                val scope      = rememberCoroutineScope()

                var opinion         by remember { mutableStateOf<Opinion?>(null) }
                var allComments     by remember { mutableStateOf<List<Comment>>(emptyList()) }
                var selectedComment by remember { mutableStateOf<Comment?>(null) }

                // 1) Load opinion + its comments
                LaunchedEffect(opinionId) {
                    vm.getOpinion(opinionId) { op -> opinion = op }
                    commentsVm.getCommentsByOpinion(opinionId)?.let { allComments = it }
                }

                // 2) Top-level comments only
                val topComments = allComments.filter { it.parentCommentId == null }

                if (opinion == null) {

                        CircularProgressIndicator()

                } else {
                    if (selectedComment == null) {

                        OpinionScreen(
                            opinion        = opinion!!,
                            comments       = allComments,
                            author         = "Ballerina Cappuccina",
                            onBack         = { finish() },
                            onReactOpinion = { _, _ -> /* … */ },
                            onReactComment = { id, like ->
                                scope.launch {
                                    commentsVm.reactToComment(id, like)
                                    commentsVm.getCommentsByOpinion(opinionId)
                                        ?.let { allComments = it }
                                }
                            },
                            onCommentClick = { clicked ->
                                selectedComment = clicked
                            },
                            onSubmitReply  = { parentId, text ->
                                scope.launch {
                                    commentsVm.createComment(
                                        Comment(
                                            id              = UUID.randomUUID(),
                                            opinionId       = UUID.fromString(opinionId),
                                            userId          = UUID.fromString("a69b8063-e6c4-4170-a9aa-eee50c40bff5"),
                                            text            = text,
                                            timestamp       = LocalDateTime.now(),
                                            likes           = 0,
                                            dislikes        = 0,
                                            parentCommentId = null
                                        )
                                    )?.let {
                                        commentsVm.getCommentsByOpinion(opinionId)
                                            ?.let { allComments = it }
                                    }
                                }
                            }
                        )
                    } else {
                        // — Replies screen —
                        RepliesScreen(
                            parent         = selectedComment!!,
                            allComments    = allComments,
                            onBack         = { selectedComment = null },
                            onReactComment = { id, like ->
                                scope.launch {
                                    commentsVm.reactToComment(id, like)
                                    commentsVm.getCommentsByOpinion(opinionId)
                                        ?.let { allComments = it }
                                }
                            },
                            onSubmitReply  = { parentId, text ->
                                scope.launch {
                                    commentsVm.createComment(
                                        Comment(
                                            id              = UUID.randomUUID(),
                                            opinionId       = UUID.fromString(opinionId),
                                            userId          = UUID.fromString("a69b8063-e6c4-4170-a9aa-eee50c40bff5"),
                                            text            = text,
                                            timestamp       = LocalDateTime.now(),
                                            likes           = 0,
                                            dislikes        = 0,
                                            parentCommentId = UUID.fromString(parentId)
                                        )
                                    )?.let {
                                        commentsVm.getCommentsByOpinion(opinionId)
                                            ?.let { allComments = it }
                                    }

                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
