import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.openline.R

@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
@Composable
fun MudSplashAnimation(
    isTriggered: Boolean,
    onAnimationComplete: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Convert dp to px for accurate animation
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val mudSizePx = with(density) { 300.dp.toPx() }

    var showImpact by remember { mutableStateOf(false) }
    var showSlide by remember { mutableStateOf(false) }

    // Center start, so we move to the bottom
    val slideOffset by animateFloatAsState(
        targetValue = if (showSlide) (screenHeightPx / 2f + (screenHeightPx / 2f - mudSizePx / 2f)) else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        finishedListener = {
            if (showSlide && it > 0f) {
                onAnimationComplete()
                showImpact = false
                showSlide = false
            }
        }
    )

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            showImpact = true
            delay(400)
            showSlide = true
        }
    }

    if (showImpact || showSlide) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.stain),
                contentDescription = "Mud Splash",
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        translationY = slideOffset
                    }
            )
        }
    }
}
