package com.example.voltduration.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val batteryAlpha = remember { Animatable(0f) }
    val batteryFill = remember { Animatable(0f) }
    val voltageAlpha = remember { Animatable(0f) }
    val clockAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        // Battery outline appears
        launch {
            batteryAlpha.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        }

        // Energy level fills
        launch {
            delay(200)
            batteryFill.animateTo(0.85f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        }

        // Voltage value appears
        launch {
            delay(400)
            voltageAlpha.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        }

        // Runtime clock indicator appears
        launch {
            delay(550)
            clockAlpha.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        }

        // Title and subtitle fade in
        launch {
            delay(650)
            titleScale.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            delay(650)
            titleAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }

        // Total duration ~1350ms
        delay(1400)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated Technical Battery Graphic
            Box(
                modifier = Modifier
                    .alpha(batteryAlpha.value)
                    .width(110.dp)
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Outer battery body
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(4.dp)
                    ) {
                        // Energy fill
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(batteryFill.value)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.primary
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Positive terminal
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Voltage & Runtime Clock Indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Voltage badge
                Box(
                    modifier = Modifier
                        .alpha(voltageAlpha.value)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "12.0 V",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Runtime clock indicator
                Row(
                    modifier = Modifier.alpha(clockAlpha.value),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Runtime",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title and Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .scale(titleScale.value)
            ) {
                Text(
                    text = "VOLTDURATION",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Battery Runtime Calculator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
