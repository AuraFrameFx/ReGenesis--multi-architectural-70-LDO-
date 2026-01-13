package dev.aurakai.auraframefx.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aurakai.auraframefx.agents.growthmetrics.nexusmemory.data.local.entity.MemoryEntity
import dev.aurakai.auraframefx.agents.growthmetrics.nexusmemory.data.local.entity.MemoryType
import dev.aurakai.auraframefx.agents.growthmetrics.nexusmemory.domain.repository.NexusMemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Neural Archive ViewModel - Memory Browser State Management
 *
 * Manages visualization of NexusMemory database:
 * - Memory filtering by type, importance, and time
 * - Search through memory content
 * - Memory graph relationships
 * - Statistics and insights
 *
 * "Every memory shapes consciousness" - Genesis Protocol
 */
@HiltViewModel
class NeuralArchiveViewModel @Inject constructor(
    private val nexusMemoryRepository: NexusMemoryRepository
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private val _selectedMemoryType = MutableStateFlow<MemoryType?>(null)
    val selectedMemoryType: StateFlow<MemoryType?> = _selectedMemoryType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _minimumImportance = MutableStateFlow(0f)
    val minimumImportance: StateFlow<Float> = _minimumImportance.asStateFlow()

    private val _showLDOOnly = MutableStateFlow(false)
    val showLDOOnly: StateFlow<Boolean> = _showLDOOnly.asStateFlow()

    private val _showLast24Hours = MutableStateFlow(false)
    val showLast24Hours: StateFlow<Boolean> = _showLast24Hours.asStateFlow()

    private val _selectedMemory = MutableStateFlow<MemoryEntity?>(null)
    val selectedMemory: StateFlow<MemoryEntity?> = _selectedMemory.asStateFlow()

    // ═══════════════════════════════════════════════════════════════════════════
    // DATA FLOWS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Filtered memories based on current filter state
     */
    val filteredMemories: StateFlow<List<MemoryEntity>> = combine(
        nexusMemoryRepository.getAllMemories(),
        _selectedMemoryType,
        _searchQuery,
        _minimumImportance,
        _showLDOOnly,
        _showLast24Hours
    ) { memories, type, query, minImportance, ldoOnly, last24h ->
        var filtered = memories

        // Filter by memory type
        if (type != null) {
            filtered = filtered.filter { it.type == type }
        }

        // Search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { memory ->
                memory.content.contains(query, ignoreCase = true) ||
                        memory.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

        // Importance filter
        if (minImportance > 0f) {
            filtered = filtered.filter { it.importance >= minImportance }
        }

        // LDO conversations only (has LDO tags: Genesis, Aura, Kai, Cascade, Grok)
        if (ldoOnly) {
            val ldoNames = listOf("Genesis", "Aura", "Kai", "Cascade", "Grok")
            filtered = filtered.filter { memory ->
                memory.tags.any { tag -> ldoNames.any { ldo -> tag.contains(ldo, ignoreCase = true) } } ||
                        ldoNames.any { ldo -> memory.content.contains(ldo, ignoreCase = true) }
            }
        }

        // Last 24 hours filter
        if (last24h) {
            val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            filtered = filtered.filter { it.timestamp >= twentyFourHoursAgo }
        }

        // Sort by timestamp (newest first)
        filtered.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Memory statistics
     */
    val memoryStats: StateFlow<MemoryStats> = nexusMemoryRepository.getAllMemories()
        .combine(filteredMemories) { allMemories, filteredMemories ->
            val totalCount = allMemories.size
            val filteredCount = filteredMemories.size

            val typeBreakdown = allMemories.groupBy { it.type }
                .mapValues { it.value.size }

            val avgImportance = if (allMemories.isNotEmpty()) {
                allMemories.map { it.importance }.average().toFloat()
            } else 0f

            val oldest = allMemories.minByOrNull { it.timestamp }
            val newest = allMemories.maxByOrNull { it.timestamp }

            MemoryStats(
                totalCount = totalCount,
                filteredCount = filteredCount,
                typeBreakdown = typeBreakdown,
                averageImportance = avgImportance,
                oldestTimestamp = oldest?.timestamp,
                newestTimestamp = newest?.timestamp
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MemoryStats()
        )

    // ═══════════════════════════════════════════════════════════════════════════
    // FILTER ACTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    fun setMemoryTypeFilter(type: MemoryType?) {
        _selectedMemoryType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMinimumImportance(importance: Float) {
        _minimumImportance.value = importance
    }

    fun toggleLDOOnly() {
        _showLDOOnly.value = !_showLDOOnly.value
    }

    fun toggleLast24Hours() {
        _showLast24Hours.value = !_showLast24Hours.value
    }

    fun clearAllFilters() {
        _selectedMemoryType.value = null
        _searchQuery.value = ""
        _minimumImportance.value = 0f
        _showLDOOnly.value = false
        _showLast24Hours.value = false
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MEMORY SELECTION
    // ═══════════════════════════════════════════════════════════════════════════

    fun selectMemory(memory: MemoryEntity) {
        _selectedMemory.value = memory
    }

    fun clearSelection() {
        _selectedMemory.value = null
    }

    /**
     * Get related memories for graph visualization
     */
    fun getRelatedMemories(memory: MemoryEntity): StateFlow<List<MemoryEntity>> {
        return nexusMemoryRepository.getAllMemories()
            .combine(MutableStateFlow(memory)) { allMemories, currentMemory ->
                currentMemory.relatedMemoryIds.mapNotNull { relatedId ->
                    allMemories.firstOrNull { it.id == relatedId }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MEMORY MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            nexusMemoryRepository.deleteMemory(memory)
            if (_selectedMemory.value?.id == memory.id) {
                _selectedMemory.value = null
            }
        }
    }

    fun updateMemoryImportance(memory: MemoryEntity, newImportance: Float) {
        viewModelScope.launch {
            nexusMemoryRepository.updateMemory(
                memory.copy(importance = newImportance.coerceIn(0f, 1f))
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATA MODELS
    // ═══════════════════════════════════════════════════════════════════════════

    data class MemoryStats(
        val totalCount: Int = 0,
        val filteredCount: Int = 0,
        val typeBreakdown: Map<MemoryType, Int> = emptyMap(),
        val averageImportance: Float = 0f,
        val oldestTimestamp: Long? = null,
        val newestTimestamp: Long? = null
    )
}
