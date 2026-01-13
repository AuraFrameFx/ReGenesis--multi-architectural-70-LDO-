package dev.aurakai.auraframefx.ui.gates

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.aurakai.auraframefx.data.repositories.AgentRepository
import dev.aurakai.auraframefx.navigation.GenesisRoutes
import kotlin.math.cos
import kotlin.math.sin

/**
 * Agent Node Colors - Genesis Protocol
 */
object AgentColors {
    val Aura = Color(0xFF00FFFF)        // Cyan - Creative Sword
    val Kai = Color(0xFFFF00FF)         // Magenta - Sentinel Shield
    val Genesis = listOf(Color(0xFF00FFFF), Color(0xFFFF00FF)) // Gradient
    val Claude = Color(0xFF0000FF)      // Deep Blue - Architect
    val Cascade = Color(0xFF87CEEB)     // Light Blue - Refiner
    val Grok = Color(0xFFFF6600)        // Neon Orange - Maverick
    val Gemini = listOf(Color(0xFF4285F4), Color(0xFFEA4335), Color(0xFFFBBC04))
    val Nemotron = Color(0xFF76B900)    // NVIDIA Green
    val MetaInstruct = Color(0xFF0668E1) // Meta Blue
}

/**
 * Hexagonal Agent Hub - Honeycomb Grid
 * Center: Nexus Memory Core (Evolution Tree)
 * Surrounding: 8 AI Agents
 */
@Composable
fun HexagonalAgentHub(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val agents = AgentRepository.getAllAgents()
    
    val infiniteTransition = rememberInfiniteTransition(label = "hex_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CarbonFiberBackground()
        
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val hexSize = minOf(maxWidth.value, maxHeight.value) / 5
            
            val hexPositions = listOf(
                Offset(-hexSize * 0.9f, -hexSize * 1.6f),
                Offset(hexSize * 0.9f, -hexSize * 1.6f),
                Offset(-hexSize * 1.8f, 0f),
                Offset(0f, 0f), // CENTER
                Offset(hexSize * 1.8f, 0f),
                Offset(-hexSize * 0.9f, hexSize * 1.6f),
                Offset(hexSize * 0.9f, hexSize * 1.6f),
            )
            
            val agentPositions = listOf(
                agents.find { it.name == "Genesis" },
                agents.find { it.name == "Claude" },
                agents.find { it.name == "Kai" },
                null, // CENTER - Nexus Core
                agents.find { it.name == "Aura" },
                agents.find { it.name == "Cascade" },
                agents.find { it.name == "Grok" },
            )
            
            hexPositions.forEachIndexed { index, position ->
                val agent = agentPositions.getOrNull(index)
                val isCenter = index == 3
                
                Box(
                    modifier = Modifier
                        .offset(x = position.x.dp, y = position.y.dp)
                        .size(hexSize.dp)
                        .clickable {
                            if (isCenter) {
                                navController.navigate(GenesisRoutes.EVOLUTION_TREE)
                            } else if (agent != null) {
                                navigateToAgentConstellation(navController, agent.name)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCenter) {
                        NexusCoreHexagon(size = hexSize, glowAlpha = glowAlpha)
                    } else if (agent != null) {
                        AgentHexagon(
                            agentName = agent.name,
                            agentColor = agent.color,
                            size = hexSize,
                            glowAlpha = glowAlpha
                        )
                    }
                }
            }
        }
        
        NavigationChevrons(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            glowAlpha = glowAlpha
        )
        
        VerticalLabel(
            text = "LDO spheregrid",
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
        )
    }
}

@Composable
private fun AgentHexagon(
    agentName: String,
    agentColor: Color,
    size: Float,
    glowAlpha: Float
) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val centerX = size / 2
        val centerY = size / 2
        val radius = size * 0.45f
        
        drawHexagonPath(centerX, centerY, radius + 8f, agentColor.copy(alpha = glowAlpha * 0.3f), Stroke(12f))
        drawHexagonPath(centerX, centerY, radius, agentColor.copy(alpha = glowAlpha), Stroke(3f))
        drawHexagonFilled(centerX, centerY, radius - 4f, agentColor.copy(alpha = 0.1f))
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = getAgentIcon(agentName), fontSize = (size * 0.25f).sp, color = agentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = agentName.uppercase(),
            fontSize = (size * 0.08f).sp,
            fontWeight = FontWeight.Bold,
            color = agentColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NexusCoreHexagon(size: Float, glowAlpha: Float) {
    val cyanColor = Color(0xFF00FFFF)
    
    Canvas(modifier = Modifier.size(size.dp)) {
        val centerX = size / 2
        val centerY = size / 2
        val radius = size * 0.45f
        
        drawHexagonPath(centerX, centerY, radius + 10f, cyanColor.copy(alpha = glowAlpha * 0.4f), Stroke(15f))
        drawHexagonPath(centerX, centerY, radius, cyanColor.copy(alpha = glowAlpha), Stroke(4f))
        drawCircuitTree(centerX, centerY, radius * 0.6f, cyanColor.copy(alpha = glowAlpha))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagonPath(
    cx: Float, cy: Float, r: Float, color: Color, style: Stroke
) {
    val path = Path()
    for (i in 0..5) {
        val angle = Math.toRadians((60.0 * i) - 30.0)
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = style)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagonFilled(
    cx: Float, cy: Float, r: Float, color: Color
) {
    val path = Path()
    for (i in 0..5) {
        val angle = Math.toRadians((60.0 * i) - 30.0)
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCircuitTree(
    cx: Float, cy: Float, r: Float, color: Color
) {
    val sw = 2f
    drawLine(color, Offset(cx, cy + r * 0.8f), Offset(cx, cy - r * 0.3f), sw)
    val by = cy - r * 0.1f
    drawLine(color, Offset(cx, by), Offset(cx - r * 0.5f, by - r * 0.4f), sw)
    drawLine(color, Offset(cx, by), Offset(cx + r * 0.5f, by - r * 0.4f), sw)
    val nr = r * 0.08f
    drawCircle(color, nr, Offset(cx - r * 0.5f, by - r * 0.4f))
    drawCircle(color, nr, Offset(cx + r * 0.5f, by - r * 0.4f))
    drawCircle(color, nr, Offset(cx, cy - r * 0.3f))
}

@Composable
private fun CarbonFiberBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gs = 20f
        for (x in 0..size.width.toInt() step gs.toInt()) {
            for (y in 0..size.height.toInt() step gs.toInt()) {
                drawRect(Color(0xFF0A0A0A), Offset(x.toFloat(), y.toFloat()), 
                    androidx.compose.ui.geometry.Size(gs - 1, gs - 1))
            }
        }
    }
}

@Composable
private fun NavigationChevrons(modifier: Modifier, glowAlpha: Float) {
    Canvas(modifier = modifier.size(40.dp, 30.dp)) {
        val color = Color(0xFF00FFFF).copy(alpha = glowAlpha)
        drawPath(Path().apply { moveTo(15f, 0f); lineTo(0f, 15f); lineTo(15f, 30f) }, 
            color, style = Stroke(3f))
        drawPath(Path().apply { moveTo(30f, 0f); lineTo(15f, 15f); lineTo(30f, 30f) }, 
            color, style = Stroke(3f))
    }
}

@Composable
private fun VerticalLabel(text: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        text.forEach { Text(it.toString(), color = Color(0xFF00FFFF).copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    }
}

private fun getAgentIcon(name: String) = when (name.lowercase()) {
    "aura" -> "⚔️"; "kai" -> "🛡️"; "genesis" -> "🔥"; "claude" -> "⚙️"
    "cascade" -> "💎"; "grok" -> "🌀"; "gemini" -> "✨"; "nemotron" -> "🧭"
    "metainstruct" -> "📚"; else -> "◆"
}

private fun navigateToAgentConstellation(navController: NavController, agentName: String) {
    val route = when (agentName.lowercase()) {
        "aura" -> "constellation"; "kai" -> "kai_constellation"; "genesis" -> "genesis_constellation"
        "claude" -> "claude_constellation"; "cascade" -> "cascade_constellation"; "grok" -> "grok_constellation"
        else -> GenesisRoutes.SPHERE_GRID
    }
    navController.navigate(route)
}
