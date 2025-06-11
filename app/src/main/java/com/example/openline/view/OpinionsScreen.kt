package com.example.openline.view

import MudSplashAnimation
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.app.ui.theme.*
import com.example.openline.R
import com.example.openline.model.Opinion
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OpinionScreen(
    opinion: Opinion,
    author: String,
    userReaction: Boolean?,
    onBack: () -> Unit,
    onReactOpinion: (String, Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var shouldShowFullScreenBubbles by remember { mutableStateOf(false) }
    var isBubbleFadingOut by remember { mutableStateOf(false) }
    var shouldShowMudSplash by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1f,
        animationSpec = tween(200)
    )
    val cardRotation by animateFloatAsState(
        targetValue = dragOffset.x * 0.02f,
        animationSpec = tween(200)
    )

    val resetOffsetX by animateFloatAsState(
        targetValue = if (!isDragging) 0f else dragOffset.x,
        animationSpec = tween(300)
    )
    val resetOffsetY by animateFloatAsState(
        targetValue = if (!isDragging) 0f else dragOffset.y,
        animationSpec = tween(300)
    )

    // Smooth bubble fade out animation
    val bubbleAlpha by animateFloatAsState(
        targetValue = if (shouldShowFullScreenBubbles && !isBubbleFadingOut) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isBubbleFadingOut) 1000 else 300, // Slower fade out
            easing = if (isBubbleFadingOut) FastOutSlowInEasing else LinearEasing
        ),
        finishedListener = {
            if (isBubbleFadingOut && it == 0f) {
                shouldShowFullScreenBubbles = false
                isBubbleFadingOut = false
            }
        }
    )

    LaunchedEffect(resetOffsetX, resetOffsetY, isDragging) {
        if (!isDragging) {
            dragOffset = Offset(resetOffsetX, resetOffsetY)
        }
    }

    val dragThresholdPx = with(density) { 60.dp.toPx() }

    val totalVotes = opinion.likes + opinion.dislikes
    val disagreePercentage = if (totalVotes > 0) (opinion.dislikes.toFloat() / totalVotes * 100) else 50f
    val agreePercentage = if (totalVotes > 0) (opinion.likes.toFloat() / totalVotes * 100) else 50f

    val animatedDisagreePercentage by animateFloatAsState(
        targetValue = disagreePercentage,
        animationSpec = tween(durationMillis = 500)
    )

    fun triggerFullScreenBubbles() {
        shouldShowFullScreenBubbles = true
        isBubbleFadingOut = false
    }

    fun triggerMudSplash() {
        shouldShowMudSplash = true
    }

    LaunchedEffect(shouldShowFullScreenBubbles) {
        if (shouldShowFullScreenBubbles && !isBubbleFadingOut) {
            // Show bubbles for 3 seconds, then start fade out
            delay(3000)
            isBubbleFadingOut = true
        }
    }

    // Full screen container with bubble wash as background
    Box(modifier = Modifier.fillMaxSize()) {
        // Background design circles
        Box(
            Modifier
                .size(300.dp)
                .offset(x = (-200).dp, y = (-200).dp)
                .background(Color(85 / 255f, 138 / 255f, 183 / 255f), CircleShape)
                .alpha(0.4f)
        )
        Box(
            Modifier
                .size(400.dp)
                .offset(x = 200.dp, y = 150.dp)
                .background(Color(105 / 255f, 165 / 255f, 148 / 255f), CircleShape)
                .alpha(0.4f)
        )

        // Main content with Scaffold
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Opinion for \"Item\"") },
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
            containerColor = Color.Transparent, // Make scaffold background transparent
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isDragging) 12.dp else 6.dp
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(if (isDragging) 10f else 1f)
                                    .graphicsLayer {
                                        translationX = dragOffset.x
                                        translationY = dragOffset.y
                                        scaleX = cardScale
                                        scaleY = cardScale
                                        rotationZ = cardRotation
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                isDragging = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDragEnd = {
                                                if (isDragging) {
                                                    val threshold = 120.dp.toPx()
                                                    when {
                                                        dragOffset.x < -threshold -> {
                                                            triggerFullScreenBubbles()
                                                            onReactOpinion(opinion.id.toString(), true)
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                        dragOffset.x > threshold -> {
                                                            triggerMudSplash()
                                                            onReactOpinion(opinion.id.toString(), false)
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                    }
                                                    isDragging = false
                                                    dragOffset = Offset.Zero
                                                }
                                            },
                                            onDrag = { _, dragAmount ->
                                                dragOffset += Offset(dragAmount.x, dragAmount.y)
                                            }
                                        )
                                    }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        opinion.text,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 26.sp
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(35.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "$author • ${opinion.timestamp.toLocalTime()}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "${disagreePercentage.toInt()}%",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp
                                                ),
                                                color = Color(0xFF8B4513)
                                            )
                                            Text(
                                                "${agreePercentage.toInt()}%",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp
                                                ),
                                                color = Color(0xFF4A90E2)
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        val animatedDisagreeWidth by animateFloatAsState(
                                            targetValue = animatedDisagreePercentage / 100f,
                                            animationSpec = tween(500)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(32.dp)
                                                .background(
                                                    Color.LightGray.copy(alpha = 0.3f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(animatedDisagreeWidth)
                                                    .background(
                                                        Color(0xFF8B4513),
                                                        RoundedCornerShape(
                                                            topStart = 16.dp,
                                                            bottomStart = 16.dp,
                                                            topEnd = if (animatedDisagreeWidth >= 0.99f) 16.dp else 0.dp,
                                                            bottomEnd = if (animatedDisagreeWidth >= 0.99f) 16.dp else 0.dp
                                                        )
                                                    )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(1f - animatedDisagreeWidth)
                                                    .align(Alignment.CenterEnd)
                                                    .background(
                                                        Color(0xFF4A90E2),
                                                        RoundedCornerShape(
                                                            topEnd = 16.dp,
                                                            bottomEnd = 16.dp,
                                                            topStart = if (animatedDisagreeWidth <= 0.01f) 16.dp else 0.dp,
                                                            bottomStart = if (animatedDisagreeWidth <= 0.01f) 16.dp else 0.dp
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val brushScale by animateFloatAsState(
                                targetValue = if (isDragging && dragOffset.x < -dragThresholdPx) 1.3f else 1f,
                                animationSpec = tween(200)
                            )
                            val brushAlpha by animateFloatAsState(
                                targetValue = if (isDragging && dragOffset.x < -dragThresholdPx) 1f else 0.6f,
                                animationSpec = tween(200)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .scale(brushScale)
                                    .alpha(brushAlpha)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.brush),
                                    contentDescription = "Brush - Like",
                                    modifier = Modifier.size(132.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "LIKE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AgreeGreen
                                )
                                Text(
                                    "${opinion.likes}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(64.dp))
                            val mudScale by animateFloatAsState(
                                targetValue = if (isDragging && dragOffset.x > dragThresholdPx) 1.3f else 1f,
                                animationSpec = tween(200)
                            )
                            val mudAlpha by animateFloatAsState(
                                targetValue = if (isDragging && dragOffset.x > dragThresholdPx) 1f else 0.6f,
                                animationSpec = tween(200)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .scale(mudScale)
                                    .alpha(mudAlpha)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.stain),
                                    contentDescription = "Mud - Dislike",
                                    modifier = Modifier.size(132.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "DISLIKE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = DisagreeRed
                                )
                                Text(
                                    "${opinion.dislikes}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                            }
                        }
                        Text(
                            "Drag the opinion card to vote!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                                .alpha(if (isDragging) 0.3f else 0.7f)
                        )
                    }
                }
            }
        )

        // Full screen bubble wash - show when triggered with animated alpha
        // Placed after Scaffold to overlay on top
        if (shouldShowFullScreenBubbles) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(bubbleAlpha)
                    .zIndex(100f) // Ensure it's on top
            ) {
                FullScreenBubbleWash(
                    bubbleCount = 180,
                    minBubbleSize = 30.dp,
                    maxBubbleSize = 200.dp,
                    animationDurationMs = 1500,
                    pauseDurationMs = 2500,
                    maxAlpha = 0.9f
                )
            }
        }

        // Mud splash animation - show when triggered
        // Positioned to fill entire screen height and overlay on top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(99f) // Ensure it's on top of content but below bubbles
        ) {
            MudSplashAnimation(
                isTriggered = shouldShowMudSplash,
                onAnimationComplete = {
                    shouldShowMudSplash = false
                }
            )
        }
    }
}