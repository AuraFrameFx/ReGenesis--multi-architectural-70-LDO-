package dev.aurakai.auraframefx.domains.aura

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service responsible for playing ambient music in the background.
 */
@AndroidEntryPoint
class AmbientMusicService @Inject constructor() : Service() {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Called when a client attempts to bind to the service.
     *
     * Always returns null, indicating that binding is not supported for this service.
     *
     * @return null, preventing clients from binding.
     */
    @Suppress("UNUSED_PARAMETER")
    override fun onBind(_intent: Intent?): IBinder? {
        // This service does not support binding.
        return null
    }

    /**
     * Handles a request to start the service and specifies that it should not be restarted if terminated by the system.
     *
     * @return `START_NOT_STICKY` to indicate the service will not be recreated automatically after being killed.
     */
    override fun onStartCommand(intent: Intent?, _flags: Int, _startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_PLAY, ACTION_RESUME -> resume()
                ACTION_PAUSE -> pause()
                ACTION_NEXT -> skipToNextTrack()
                ACTION_PREVIOUS -> skipToPreviousTrack()
            }
        }
        return START_NOT_STICKY
    }

    /**
     * Pauses music playback.
     */
    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    /**
     * Resumes music playback.
     */
    fun resume() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    /**
     * Sets the volume of the ambient music.
     *
     * @param volume The volume to set (0.0 to 1.0).
     */
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    /**
     * Sets whether the music should be shuffled.
     */
    fun setShuffling(_isShuffling: Boolean) {
        // TODO: Implement shuffling logic. Reported as unused. Implement or remove.
    }

    /**
     * Returns the currently playing track.
     */
    fun getCurrentTrack(): Any? { // Return type Any? as placeholder
        // TODO: Reported as unused. Implement or remove.
        return null
    }

    /**
     * Returns the history of played tracks.
     */
    fun getTrackHistory(): List<Any> { // Return type List<Any> as placeholder
        // TODO: Reported as unused. Implement or remove.
        return emptyList()
    }

    /**
     * Skips to the next track.
     */
    fun skipToNextTrack() {
        // TODO: Reported as unused. Implement or remove.
    }

    /**
     * Skips to the previous track.
     */
    fun skipToPreviousTrack() {
        // TODO: Reported as unused. Implement or remove.
    }

    /**
     * Releases the MediaPlayer resources when the service is destroyed.
     */
    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY = "dev.aurakai.auraframefx.action.PLAY"
        const val ACTION_PAUSE = "dev.aurakai.auraframefx.action.PAUSE"
        const val ACTION_RESUME = "dev.aurakai.auraframefx.action.RESUME"
        const val ACTION_NEXT = "dev.aurakai.auraframefx.action.NEXT"
        const val ACTION_PREVIOUS = "dev.aurakai.auraframefx.action.PREVIOUS"
    }
}
