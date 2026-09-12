package com.bob.whopaidit.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bob.whopaidit.R
import com.bob.whopaidit.ui.theme.Neutral
import com.bob.whopaidit.ui.theme.WhoPaidItTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        delay(2500.milliseconds)
        onSplashFinished()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_wallet),
                    contentDescription = "App Wallet Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WhoPaidIt",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Track, Split, Settle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Neutral,
                    letterSpacing = 0.5.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThreeDotsLoadingAnimation(
                    dotSize = 10.dp,
                    dotColor = MaterialTheme.colorScheme.primary,
                    spaceBetween = 8.dp,
                    travelDistance = 8.dp,
                )
            }
        }
    }
}

@Composable
fun ThreeDotsLoadingAnimation(
    modifier: Modifier = Modifier,
    dotSize: Dp = 10.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    spaceBetween: Dp = 8.dp,
    travelDistance: Dp = 8.dp,
) {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) },
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay((index * 150).milliseconds)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 using LinearEasing
                        1.0f at 300 using FastOutSlowInEasing
                        0.0f at 600 using FastOutSlowInEasing
                        0.0f at 1200 using LinearEasing
                    },
                    repeatMode = RepeatMode.Restart,
                ),
            )
        }
    }

    val density = LocalDensity.current
    val travelDistancePx = with(density) { travelDistance.toPx() }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        translationY = -animatable.value * travelDistancePx
                        alpha = 0.3f + (animatable.value * 0.7f)
                    }
                    .background(
                        color = dotColor,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    WhoPaidItTheme {
        SplashScreen()
    }
}
