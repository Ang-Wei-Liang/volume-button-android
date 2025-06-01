package com.example.detectvolumebuttonandroid

import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.util.Log
import kotlinx.coroutines.*

class VolumeButtonHelper( //part 5
    private var context: Context,
    private var stream: Int? = null,
    enabledScreenOff: Boolean
) {
    companion object {
        const val TAG = "VolumeButtonHelper"
        const val VOLUME_CHANGE_ACTION = "android.media.VOLUME_CHANGED_ACTION"
        const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
    }

    enum class Direction {
        Up, Down, Release
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var volumeBroadCastReceiver: VolumeBroadCastReceiver? = null
    private var volumeChangeListener: VolumeChangeListener? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var priorVolume = -1
    private var volumePushes = 0.0
    private var longPressReported = false

    var doublePressTimeout = 350L
    var buttonReleaseTimeout = 100L

    var minVolume = -1
        private set
    var maxVolume = -1
        private set
    var halfVolume = -1
        private set
    var currentVolume = -1
        private set

    init {
        audioManager?.let {
            minVolume = it.getStreamMinVolume(AudioManager.STREAM_MUSIC)
            maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            halfVolume = (minVolume + maxVolume) / 2

            if (enabledScreenOff) {
                mediaPlayer = MediaPlayer.create(context, R.raw.silence).apply {
                    isLooping = true
                    setWakeMode(context, PARTIAL_WAKE_LOCK)
                    start()
                }
            }
        } ?: Log.e(TAG, "Unable to initialize AudioManager")
    }

    fun registerVolumeChangeListener(listener: VolumeChangeListener) {
        if (volumeBroadCastReceiver == null) {
            this.volumeChangeListener = listener
            volumeBroadCastReceiver = VolumeBroadCastReceiver()
            val filter = IntentFilter().apply {
                addAction(VOLUME_CHANGE_ACTION)
            }
            context.registerReceiver(volumeBroadCastReceiver, filter)
        }
    }

    fun unregisterReceiver() {
        volumeBroadCastReceiver?.let {
            context.unregisterReceiver(it)
            volumeBroadCastReceiver = null
        }
    }

    fun onVolumePress(count: Int) {
        when (count) {
            1 -> volumeChangeListener?.onSinglePress()
            2 -> volumeChangeListener?.onDoublePress()
            else -> volumeChangeListener?.onVolumePress(count)
        }
    }

    interface VolumeChangeListener {
        fun onVolumeChange(direction: Direction)
        fun onVolumePress(count: Int)
        fun onSinglePress()
        fun onDoublePress()
        fun onLongPress()
    }

    inner class VolumeBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (stream == null || intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) == stream) {
                currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: -1
                if (currentVolume != -1 && currentVolume != priorVolume) {
                    val direction = if (currentVolume > priorVolume) Direction.Up else Direction.Down
                    volumeChangeListener?.onVolumeChange(direction)
                    priorVolume = currentVolume

                    volumePushes += 0.5
                    if (volumePushes == 0.5) {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(doublePressTimeout - buttonReleaseTimeout)
                            buttonDown()
                        }
                    }
                }
            }
        }

        private fun buttonDown() {
            val startVolumePushes = volumePushes
            CoroutineScope(Dispatchers.Main).launch {
                delay(buttonReleaseTimeout)
                if (startVolumePushes != volumePushes) {
                    if (volumePushes > 2 && !longPressReported) {
                        longPressReported = true
                        volumeChangeListener?.onLongPress()
                    }
                    buttonDown()
                } else {
                    onVolumePress(volumePushes.toInt())
                    volumeChangeListener?.onVolumeChange(Direction.Release)
                    volumePushes = 0.0
                    longPressReported = false
                }
            }
        }
    }
}