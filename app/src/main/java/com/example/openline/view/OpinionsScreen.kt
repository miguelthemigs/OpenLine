package com.example.openline.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import com.example.openline.utils.timeAgo
import kotlin.math.abs

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

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

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

    LaunchedEffect(resetOffsetX, resetOffsetY, isDragging) {
        if (!isDragging) {
            dragOffset = Offset(resetOffsetX, resetOffsetY)
        }
    }

    val density = LocalDensity.current
    val dragThresholdPx = with(density) { 60.dp.toPx() }

    // Calculate actual percentages based on current likes/dislikes
    val totalVotes = opinion.likes + opinion.dislikes
    val disagreePercentage = if (totalVotes > 0) (opinion.dislikes.toFloat() / totalVotes * 100) else 50f
    val agreePercentage = if (totalVotes > 0) (opinion.likes.toFloat() / totalVotes * 100) else 50f

    // Animated percentage for the bar (only animate after vote changes, not during drag)
    val animatedDisagreePercentage by animateFloatAsState(
        targetValue = disagreePercentage,
        animationSpec = tween(durationMillis = 500)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background circles
        Box(
            Modifier
                .size(300.dp)
                .offset(x = -200.dp, y = -200.dp)
                .background(Color(85 / 255f, 138 / 255f, 183 / 255f), CircleShape)
                .alpha(0.6f)
        )
        Box(
            Modifier
                .size(400.dp)
                .offset(x = 200.dp, y = 150.dp)
                .background(Color(105 / 255f, 165 / 255f, 148 / 255f), CircleShape)
                .alpha(0.6f)
        )

        Column(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Opinion for \"Item\" ") },
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
                containerColor = CardBackground,
                content = { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Draggable Opinion Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(if (isDragging) 12.dp else 4.dp),
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
                                                            onReactOpinion(
                                                                opinion.id.toString(),
                                                                true
                                                            )
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                        dragOffset.x > threshold -> {
                                                            onReactOpinion(
                                                                opinion.id.toString(),
                                                                false
                                                            )
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                    }
                                                    isDragging = false
                                                    dragOffset = Offset.Zero
                                                }
                                            },
                                            onDrag = { _, dragAmount ->
                                                dragOffset += Offset(
                                                    dragAmount.x,
                                                    dragAmount.y
                                                )
                                            }
                                        )
                                    }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        opinion.text,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 32.sp
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    // Author info
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

                                    // Percentage Bar Design
                                    Column {
                                        // Percentage numbers (show actual percentages, no drag influence)
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
                                                color = Color(0xFF8B4513) // Brown color for disagree
                                            )
                                            Text(
                                                "${agreePercentage.toInt()}%",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp
                                                ),
                                                color = Color(0xFF4A90E2) // Blue color for agree
                                            )
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        // Animated percentage bar (animates based on actual vote changes)
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
                                            // Disagree section (left/brown)
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

                                            // Agree section (right/blue) - positioned from the right
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

                        // --- DRAG TARGETS ---
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

                        // Instruction text
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
            )
        }
    }
}