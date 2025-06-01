package com.example.detectvolumebuttonandroid

import android.app.*
import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent

class ForegroundService : Service() {

    private lateinit var volumeButtonHelper: VolumeButtonHelper
    private lateinit var mediaSession: MediaSessionCompat
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        var wakeLock: PowerManager.WakeLock? = null

        const val TAG = "ForegroundService"
        const val ACTION_FOREGROUND_WAKELOCK = "com.example.detectvolumebuttonandroid.ACTION_FOREGROUND_WAKELOCK"
        const val ACTION_FOREGROUND = "com.example.detectvolumebuttonandroid.ACTION_FOREGROUND"
        const val WAKELOCK_TAG = "com.example.detectvolumebuttonandroid:wake-service"
        const val CHANNEL_ID = "VolumeButtonHelperChannel"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("Recording", "Start Register VolumeButtonHelper")


        // Setup Volume Button Helper
        volumeButtonHelper = VolumeButtonHelper(this, AudioManager.STREAM_MUSIC, true)
        volumeButtonHelper.registerVolumeChangeListener(object : VolumeButtonHelper.VolumeChangeListener {
            override fun onVolumeChange(direction: VolumeButtonHelper.Direction) {
                Log.i(TAG, "onVolumeChange: $direction")
            }

            override fun onVolumePress(count: Int) {
                Log.i(TAG, "onVolumePress: $count")
            }

            override fun onSinglePress() {
                Log.i(TAG, "onSinglePress")
            }

            override fun onDoublePress() {
                Log.i(TAG, "onDoublePress")
            }

            override fun onLongPress() {
                Log.i(TAG, "onLongPress")
            }
        })

        // Setup Media Session
        setupMediaSession()

        // Start silent audio
        startSilentAudio()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used to keep the service running in background"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("SilentWitness")
            .setContentText("Listening for volume button presses")
            //.setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_FOREGROUND || intent?.action == ACTION_FOREGROUND_WAKELOCK) {
            createNotificationChannel()
            startForeground(1, buildNotification())
        }

        if (intent?.action == ACTION_FOREGROUND_WAKELOCK) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (wakeLock == null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
                wakeLock?.acquire()
            } else {
                wakeLock?.release()
                wakeLock = null
            }
        }

        return START_STICKY
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "SilentWitnessSession").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    val keyEvent = mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                    if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                        when (keyEvent.keyCode) {
                            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                Log.d(TAG, "Volume button pressed: ${keyEvent.keyCode}")
                                // Trigger your helper
                                //volumeButtonHelper.simulatePress() // implement this if needed
                            }
                        }
                    }
                    return true
                }
            })
            isActive = true

            // Required to ensure the session has transport controls
            val state = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1f)
                .build()
            setPlaybackState(state)
        }
    }

    private fun startSilentAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.silence)
        mediaPlayer?.apply {
            isLooping = true
            setVolume(0f, 0f)
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        wakeLock = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        volumeButtonHelper.unregisterReceiver()

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        mediaSession.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}



