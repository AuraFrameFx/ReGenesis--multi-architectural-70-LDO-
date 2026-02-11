package dev.aurakai.auraframefx.domains.aura.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

/**
 * A cyberpunk-themed text component that supports an optional glitch effect.
 *
 * @param text The text to display.
 * @param color The [CyberpunkTextColor] scheme to apply.
 * @param style The [CyberpunkTextStyle] to apply.
 * @param modifier Modifier to be applied to the layout.
 * @param enableGlitch Whether to enable the visual glitch animation.
 */
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
            // Randomly trigger a glitch burst
            val isGlitching = Random.nextFloat() > 0.9f
            if (isGlitching) {
                showGlitchLayers = true
                // Perform a burst of quick glitch state changes
                repeat(Random.nextInt(3, 7)) {
                    glitchOffset = Offset(
                        x = (Random.nextFloat() - 0.5f) * 14f,
                        y = (Random.nextFloat() - 0.5f) * 4f
                    )
                    glitchAlpha = if (Random.nextFloat() > 0.2f) 1f else 0.4f
                    delay(Random.nextLong(30, 80))
                }
                // Reset state after burst
                showGlitchLayers = false
                glitchOffset = Offset.Zero
                glitchAlpha = 1f
            }
            // Wait before next potential glitch burst
            delay(Random.nextLong(100, 500))
        }
    }

    Box(modifier = modifier) {
        if (showGlitchLayers) {
            // Cyberpunk Cyan glitch layer (offset in one direction)
            Text(
                text = text,
                color = CyberpunkCyan.copy(alpha = 0.6f),
                style = style.textStyle,
                modifier = Modifier.offset {
                    IntOffset(glitchOffset.x.toInt(), glitchOffset.y.toInt())
                }
            )

            // Cyberpunk Pink glitch layer (offset in opposite direction)
            Text(
                text = text,
                color = CyberpunkPink.copy(alpha = 0.6f),
                style = style.textStyle,
                modifier = Modifier.offset {
                    IntOffset(-glitchOffset.x.toInt(), -glitchOffset.y.toInt())
                }
            )
        }

        // Main text layer with potential flickering alpha
        Text(
            text = text,
            color = color.color,
            style = style.textStyle,
            modifier = Modifier.alpha(glitchAlpha)
        )
    }
}
