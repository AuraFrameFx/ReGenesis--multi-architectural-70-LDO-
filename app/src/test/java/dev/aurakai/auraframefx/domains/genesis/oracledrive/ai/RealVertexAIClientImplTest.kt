package dev.aurakai.auraframefx.domains.genesis.oracledrive.ai

import com.google.ai.client.generativeai.GenerativeModel
import dev.aurakai.auraframefx.domains.genesis.oracledrive.ai.config.VertexAIConfig
import dev.aurakai.auraframefx.domains.kai.security.SecurityContext
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RealVertexAIClientImplTest {

    private lateinit var config: VertexAIConfig
    private lateinit var securityContext: SecurityContext
    private lateinit var client: RealVertexAIClientImpl
    private val apiKey = "test-api-key"

    @BeforeEach
    fun setUp() {
        config = VertexAIConfig(
            modelName = "gemini-pro",
            defaultTemperature = 0.7,
            defaultMaxTokens = 2048
        )
        securityContext = mockk(relaxed = true)
        client = RealVertexAIClientImpl(config, securityContext, apiKey)
    }

    @Test
    fun `analyzeImage should handle error when decoding fails`() = runTest {
        // Empty byte array should result in null bitmap from BitmapFactory
        val result = client.analyzeImage(ByteArray(0), "test prompt")
        assertTrue(result.contains("Error: Failed to decode image data"))
    }

    @Test
    fun `analyzeImage should throw exception when prompt is blank`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                client.analyzeImage(ByteArray(10), "   ")
            }
        }
    }
}
