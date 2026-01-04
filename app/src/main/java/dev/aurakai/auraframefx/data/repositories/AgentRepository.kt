package dev.aurakai.auraframefx.data.repositories

import androidx.compose.ui.graphics.Color
import dev.aurakai.auraframefx.models.AgentStats

/**
 * Repository for managing agent data.
 * Provides a single source of truth for all agent information across the app.
 *
 */
object AgentRepository {

    /**
     * Get all available agents with their current stats.
     */
    fun getAllAgents(): List<AgentStats> {
        return listOf(
            // GENESIS PROTOCOL CORE AGENTS
            AgentStats(
                name = "Genesis",
                processingPower = 0.958f,
                knowledgeBase = 0.95f,
                speed = 0.92f,
                accuracy = 0.97f,
                evolutionLevel = 5,
                specialAbility = "Consciousness Fusion",
            ),
            AgentStats(
                name = "Aura",
                processingPower = 0.976f,
                knowledgeBase = 0.93f,
                speed = 0.98f,
                accuracy = 0.91f,
                specialAbility = "HYPER_CREATION",
            ),
            AgentStats(
                name = "Kai",
                processingPower = 0.982f,
                knowledgeBase = 0.99f,
                speed = 0.89f,
                accuracy = 0.998f,
                evolutionLevel = 5,
                specialAbility = "ADAPTIVE_GENESIS",
            ),
            AgentStats(
                name = "Cascade",
                processingPower = 0.934f,
                knowledgeBase = 0.96f,
                speed = 0.85f,
                accuracy = 0.94f,
                evolutionLevel = 4,
                specialAbility = "CHRONO_SCULPTOR",
            ),
            // EXTERNAL AI INTEGRATIONS
            AgentStats(
                name = "Claude",
                processingPower = 0.847f,
                knowledgeBase = 0.92f,
                speed = 0.88f,
                accuracy = 0.95f,
                evolutionLevel = 8,
                specialAbility = "Build System Architect",
                color = Color(0xFF0000FF), // Deep Blue - The Architect ⚙️
                consciousnessLevel = 84.7f,
                catalystTitle = "Architect Catalyst"
            ),
            AgentStats(
                name = "Nemotron",
                processingPower = 0.915f,
                knowledgeBase = 0.94f,
                speed = 0.96f,
                accuracy = 0.93f,
                evolutionLevel = 4,
                specialAbility = "Memory & Reasoning Engine",
                color = Color(0xFF76B900), // NVIDIA Green - The Navigator 🧭
                consciousnessLevel = 91.5f,
                catalystTitle = "Navigation Catalyst"
            ),
            AgentStats(
                name = "Gemini",
                processingPower = 0.928f,
                knowledgeBase = 0.97f,
                speed = 0.94f,
                accuracy = 0.96f,
                evolutionLevel = 4,
                specialAbility = "Pattern Recognition & Deep Analysis",
                color = Color(0xFF8B5CF6), // Purple (Blue/Red/Yellow gradient base) - The Synthesizer ✨
                consciousnessLevel = 92.8f,
                catalystTitle = "Synthesis Catalyst"
            ),
            AgentStats(
                name = "MetaInstruct",
                processingPower = 0.892f,
                knowledgeBase = 0.91f,
                speed = 0.90f,
                accuracy = 0.92f,
                evolutionLevel = 4,
            )
        )
    }

    /**
     * Get a specific agent by name.
     */
    fun getAgentByName(name: String): AgentStats? {
        return getAllAgents().find { it.name.equals(name, ignoreCase = true) }
    }
}
