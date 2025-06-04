package com.example.openline

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openline.model.Opinion
import com.example.openline.view.OpinionScreen
import com.example.openline.ui.theme.OpenLineTheme
import com.example.openline.viewmodel.OpinionsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OpenLineTheme {
                val opinionId = "9c30f864-9499-4d57-9a2b-fd2c2d427532"
                val vm: OpinionsViewModel = viewModel()
                val scope = rememberCoroutineScope()

                var opinion by remember { mutableStateOf<Opinion?>(null) }

                // Reaction state for local UI
                var userReaction by remember { mutableStateOf<Boolean?>(null) }
                var opinionLikes by remember { mutableStateOf(0) }
                var opinionDislikes by remember { mutableStateOf(0) }

                // Load opinion
                LaunchedEffect(opinionId) {
                    vm.getOpinion(opinionId) { op ->
                        opinion = op
                        if (op != null) {
                            opinionLikes = op.likes
                            opinionDislikes = op.dislikes
                        }
                        userReaction = null // or op.userReaction if your API has it
                    }
                }

                if (opinion == null) {
                    CircularProgressIndicator()
                } else {
                    OpinionScreen(
                        opinion = opinion!!.copy(
                            likes = opinionLikes,
                            dislikes = opinionDislikes
                        ),
                        author = "Ballerina Cappuccina",
                        userReaction = userReaction,
                        onBack = { finish() },
                        onReactOpinion = { id, like ->
                            // Update local state immediately for smooth UI
                            userReaction = like
                            if (like) {
                                opinionLikes += 1
                            } else {
                                opinionDislikes += 1
                            }

                            scope.launch {
                                try {
                                    // Call backend API
                                    vm.reactToOpinion(id, like)

                                    // Refresh opinion data from server to get accurate counts
                                    vm.getOpinion(id) { updated ->
                                        updated?.let {
                                            opinion = it
                                            opinionLikes = it.likes
                                            opinionDislikes = it.dislikes
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Error reacting to opinion: ${e.message}")
                                    // Revert local state on error
                                    userReaction = null
                                    if (like) {
                                        opinionLikes -= 1
                                    } else {
                                        opinionDislikes -= 1
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}