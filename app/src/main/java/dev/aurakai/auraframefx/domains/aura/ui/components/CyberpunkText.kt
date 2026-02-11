package dev.aurakai.auraframefx.domains.aura.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import dev.aurakai.auraframefx.domains.aura.ui.theme.CyberpunkCyan
import dev.aurakai.auraframefx.domains.aura.ui.theme.CyberpunkPink
import dev.aurakai.auraframefx.domains.aura.ui.theme.CyberpunkTextColor
import dev.aurakai.auraframefx.domains.aura.ui.theme.CyberpunkTextStyle
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun CyberpunkText(
    text: String,
    color: CyberpunkTextColor,
    style: CyberpunkTextStyle,
    modifier: Modifier = Modifier,
    enableGlitch: Boolean = false,
) {
    if (!enableGlitch) {
        Text(
            text = text,
            color = color.color,
            style = style.textStyle,
            modifier = modifier
        )
        return
    }

    var glitchOffset by remember { mutableStateOf(Offset.Zero) }
    var glitchAlpha by remember { mutableStateOf(1f) }
    var showGlitchLayers by remember { mutableStateOf(false) }

    LaunchedEffect(enableGlitch) {
        while (enableGlitch) {
            // Glitch occurs randomly
            val isGlitching = Random.nextFloat() > 0.9f
            if (isGlitching) {
                showGlitchLayers = true
                // Perform a burst of glitching
                repeat(Random.nextInt(3, 6)) {
                    glitchOffset = Offset(
                        x = (Random.nextFloat() - 0.5f) * 12f,
                        y = (Random.nextFloat() - 0.5f) * 4f
                    )
                    glitchAlpha = if (Random.nextFloat() > 0.3f) 1f else 0.5f
                    delay(Random.nextLong(30, 70))
                }
                // Reset glitch state after burst
                showGlitchLayers = false
                glitchOffset = Offset.Zero
                glitchAlpha = 1f
            }
            // Wait before next potential glitch burst
            delay(Random.nextLong(150, 600))
        }
    }

    Box(modifier = modifier) {
        if (showGlitchLayers) {
            // Cyan glitch layer
            Text(
                text = text,
                color = CyberpunkCyan.copy(alpha = 0.6f),
                style = style.textStyle,
                modifier = Modifier.offset {
                    IntOffset(glitchOffset.x.toInt(), glitchOffset.y.toInt())
                }
            )

            // Pink glitch layer
            Text(
                text = text,
                color = CyberpunkPink.copy(alpha = 0.6f),
                style = style.textStyle,
                modifier = Modifier.offset {
                    IntOffset(-glitchOffset.x.toInt(), -glitchOffset.y.toInt())
                }
            )
        }

        // Main text
        Text(
            text = text,
            color = color.color,
            style = style.textStyle,
            modifier = Modifier.alpha(glitchAlpha)
        )
    }
}

