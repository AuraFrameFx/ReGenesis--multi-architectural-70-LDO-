package dev.aurakai.auraframefx.ui.gates

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aurakai.auraframefx.ui.gates.components.*
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * FLOATING HUD GATE CARD
 * Complete entrance card system with:
 * - Metallic border with corner screws
 * - Circuit board background
 * - Neon pink/magenta icon in center
 * - Pixelated vertical title on right
 * - Blade Runner cyberpunk aesthetic
 */
@Composable
fun FloatingHUDGateCard(
    config: GateConfig,
    circuitStyle: CircuitStyle,
    icon: @Composable (Color, Float) -> Unit,
    modifier: Modifier = Modifier,
    onDoubleTap: () -> Unit = {}
) {
    var lastTapTime by remember { mutableLongStateOf(0L) }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "hud_animations")

    // Pulsing glow
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Circuit animation
    val circuitOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "circuit_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pure black background
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 300) {
                            onDoubleTap()
                        }
                        lastTapTime = currentTime
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Floating card container with shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .offset(y = floatOffset.dp)
        ) {
            // Drop shadow for depth
            Canvas(modifier = Modifier.fillMaxSize().offset(y = 8.dp)) {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.7f),
                    topLeft = Offset(10f, 10f),
                    size = size
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // LAYER 1: Dark background fill
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0A0A))
                )

                // LAYER 2: Circuit pattern background
                CircuitPattern(
                    style = circuitStyle,
                    primaryColor = config.borderColor,
                    pulseAlpha = pulseAlpha * 0.6f,
                    animationOffset = circuitOffset,
                    modifier = Modifier.fillMaxSize()
                )

                // LAYER 3: Metallic border with screws
                MetallicBorder(
                    primaryColor = config.borderColor,
                    accentColor = config.accentColor ?: Color(0xFFFF00FF),
                    pulseAlpha = pulseAlpha,
                    modifier = Modifier.fillMaxSize()
                )

                // LAYER 4: Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Center icon with dark frame
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dark frame behind icon
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = Color(0xFF1A1A1A),
                                topLeft = Offset(0f, 0f),
                                size = size
                            )
                            drawRoundRect(
                                color = Color(0xFF2A2A2A),
                                topLeft = Offset(4f, 4f),
                                size = androidx.compose.ui.geometry.Size(size.width - 8f, size.height - 8f)
                            )
                        }

                        // Neon icon
                        icon(config.accentColor ?: Color(0xFFFF00FF), pulseAlpha)
                    }
                }

                // LAYER 5: Vertical title on right side (pixelated style)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    PixelatedVerticalText(
                        text = config.title.uppercase().replace(" ", ""),
                        color = config.borderColor,
                        pulseAlpha = pulseAlpha
                    )
                }

                // LAYER 6: Scanline overlay
                ScanlineOverlay(
                    color = config.borderColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Pixelated vertical text (like TERMINAL, ORACLE examples)
 */
@Composable
private fun PixelatedVerticalText(
    text: String,
    color: Color,
    pulseAlpha: Float
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        text.forEach { char ->
            Box {
                // Glow layer
                Text(
                    text = char.toString(),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp
                    ),
                    color = color.copy(alpha = pulseAlpha * 0.5f),
                    modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                )

                // Main text
                Text(
                    text = char.toString(),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp
                    ),
                    color = color.copy(alpha = pulseAlpha)
                )

                // Horizontal stripes for pixelated effect
                Canvas(modifier = Modifier.matchParentSize()) {
                    for (y in 0..size.height.toInt() step 3) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.3f),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(size.width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scanline CRT overlay
 */
@Composable
private fun ScanlineOverlay(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Horizontal scanlines
        for (y in 0..size.height.toInt() step 3) {
            drawLine(
                color = color.copy(alpha = 0.05f),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // Occasional bright scanline
        for (y in 0..size.height.toInt() step 40) {
            drawLine(
                color = color.copy(alpha = 0.15f),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 2f
            )
        }
    }
}
