package com.example.openline.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.openline.R
import kotlin.random.Random

data class BubbleData(
    val x: Float,
    val y: Float,
    val size: Dp,
    val animationDelay: Int
)

@Composable
fun FullScreenBubbleWash(
    bubbleCount: Int = 25,
    minBubbleSize: Dp = 60.dp,
    maxBubbleSize: Dp = 140.dp,
    animationDurationMs: Int = 2000, // Time for fade in + visible + fade out
    pauseDurationMs: Int = 4000, // Time between cycles
    maxAlpha: Float = 1f
) {
    val configuration = LocalConfiguration.current

    // Simplified screen dimension calculation
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    // Generate random bubble data once per composition
    val bubbles = remember {
        List(bubbleCount) { index ->
            val bubbleSize = Random.nextFloat() * (maxBubbleSize.value - minBubbleSize.value) + minBubbleSize.value
            BubbleData(
                x = Random.nextFloat() * (screenWidthDp.value - bubbleSize).coerceAtLeast(0f),
                y = Random.nextFloat() * (screenHeightDp.value - bubbleSize).coerceAtLeast(0f),
                size = bubbleSize.dp,
                animationDelay = Random.nextInt(0, animationDurationMs + pauseDurationMs)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubbleWash")

    Box(modifier = Modifier.fillMaxSize()) {
        bubbles.forEach { bubble ->
            val totalCycleDuration = animationDurationMs + pauseDurationMs
            val fadeInDuration = (animationDurationMs * 0.25f).toInt()
            val visibleDuration = (animationDurationMs * 0.35f).toInt()
            val fadeOutDuration = (animationDurationMs * 0.4f).toInt() // Longer fade out for smoothness

            val alphaAnim by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalCycleDuration

                        // Start invisible
                        0f at 0 with LinearOutSlowInEasing

                        // Fade in smoothly
                        maxAlpha at fadeInDuration using FastOutSlowInEasing

                        // Stay visible
                        maxAlpha at (fadeInDuration + visibleDuration) using LinearEasing

                        // Smooth fade out - this is the key fix
                        0f at (fadeInDuration + visibleDuration + fadeOutDuration) using FastOutSlowInEasing

                        // Stay invisible during pause with explicit 0f values
                        0f at (totalCycleDuration - 100) using LinearEasing
                        0f at totalCycleDuration using LinearEasing
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(bubble.animationDelay)
                ),
                label = "bubbleAlpha_${bubble.hashCode()}"
            )

            Box(
                modifier = Modifier
                    .offset(x = bubble.x.dp, y = bubble.y.dp)
                    .size(bubble.size)
                    .alpha(alphaAnim)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bubble),
                    contentDescription = "Background Bubble",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}