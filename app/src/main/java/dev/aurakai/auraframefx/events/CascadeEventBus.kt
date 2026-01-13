package dev.aurakai.auraframefx.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus for Cascade system events.
 */
@Singleton
class CascadeEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<CascadeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<CascadeEvent> = _events

    /**
     * Publishes an event to the cascade event bus.
     *
     * The event is offered into the bus; if the internal buffer is full the event may be dropped.
     *
     * @param event The `CascadeEvent` to publish.
     */
    fun emit(event: CascadeEvent) {
        _events.tryEmit(event)
    }

    /**
     * Attempts to emit the given CascadeEvent into the internal event flow's buffer.
     *
     * @param event The CascadeEvent to publish.
     * @return `true` if the event was accepted into the flow's buffer, `false` otherwise.
     */
    fun tryEmit(event: CascadeEvent): Boolean {
        return _events.tryEmit(event)
    }
}

interface CascadeEvent

data class MemoryEvent(
    val type: String,
    val data: Map<String, Any>
) : CascadeEvent