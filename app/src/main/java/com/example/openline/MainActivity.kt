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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.openline.model.Opinion
import com.example.openline.ui.theme.OpenLineTheme
import com.example.openline.view.LoginScreen
import com.example.openline.view.OpinionScreen
import com.example.openline.view.RegisterScreen
import com.example.openline.viewmodel.AuthViewModel
import com.example.openline.viewmodel.OpinionsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OpenLineTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "login") {

                    // Login Screen
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("opinions") },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    // Register Screen
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("opinions") },
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }

                    // Opinion Screen (fully loading from backend as you already built)
                    composable("opinions") {
                        val opinionId = "9c30f864-9499-4d57-9a2b-fd2c2d427532"
                        val vm: OpinionsViewModel = viewModel()
                        val scope = rememberCoroutineScope()

                        var opinion by remember { mutableStateOf<Opinion?>(null) }

                        var userReaction by remember { mutableStateOf<Boolean?>(null) }
                        var opinionLikes by remember { mutableStateOf(0) }
                        var opinionDislikes by remember { mutableStateOf(0) }

                        LaunchedEffect(opinionId) {
                            vm.getOpinion(opinionId) { op ->
                                opinion = op
                                if (op != null) {
                                    opinionLikes = op.likes
                                    opinionDislikes = op.dislikes
                                }
                                userReaction = null
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
                                    userReaction = like
                                    if (like) {
                                        opinionLikes += 1
                                    } else {
                                        opinionDislikes += 1
                                    }

                                    scope.launch {
                                        try {
                                            vm.reactToOpinion(id, like)
                                            vm.getOpinion(id) { updated ->
                                                updated?.let {
                                                    opinion = it
                                                    opinionLikes = it.likes
                                                    opinionDislikes = it.dislikes
                                                }
                                            }
                                        } catch (e: Exception) {
                                            println("Error reacting: ${e.message}")
                                            userReaction = null
                                            if (like) opinionLikes -= 1 else opinionDislikes -= 1
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
}
