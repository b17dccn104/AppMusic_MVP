package com.example.musicappmvp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import androidx.core.content.ContextCompat
import com.example.musicappmvp.R
import com.example.musicappmvp.model.Song
import com.example.musicappmvp.notification.senNotification

class SongService : Service() {
    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var isPlayMusic: Boolean = false
    private  var updateSong: Song? = null
    private var song: Song? = null
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(
                getString(R.string.info_channel_id),
                getString(R.string.info_channel_name)
        )
    }

    override fun onBind(p0: Intent?): IBinder = SongBinder(this)
    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val bundle = intent.extras
        if (bundle != null) {
            song = bundle?.getParcelable(getString(R.string.action_intent_listsong))
            if (song != null) {
                updateSong = song
                startMusic(song as Song)
                startForceground(this, song as Song, isPlayMusic)
            }
        }
        var actionMusicFromReceiver = intent.getIntExtra(getString(R.string.action_intent_music_receiver), 0)
        handleActionMusic(actionMusicFromReceiver)
        return START_NOT_STICKY
    }

    private fun startMusic(song: Song) {
        mediaPlayer?.apply {
                setDataSource(song.SongURL)
                prepare()
                start()
        }
        isPlayMusic = true
    }

    fun isPlaying() = mediaPlayer?.isPlaying

    fun handleActionMusic(action: Int){
        when(action){
            ACTION_PAUSE -> pauseMusic()
            ACTION_RESUME -> resumeMusic()
            ACTION_CANCEL -> cancelMusic()
        }
    }

    fun cancelMusic() {
        stopSelf()
    }

    fun startForceground(context: Context, song: Song, isPlay: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                    NOTIFICATION_MUSIC_ID,
                    senNotification(
                            context,
                            song,
                            isPlay
                    )
            )
        } else notificationManager?.notify(
                NOTIFICATION_MUSIC_ID,
                senNotification(
                        context,
                        song,
                        isPlay
                )
        )
    }

   fun resumeMusic() {
        if (mediaPlayer != null && !isPlayMusic) {
            mediaPlayer?.start()
            isPlayMusic = true
            startForceground(this, updateSong as Song, isPlayMusic)
        }
   }

   fun pauseMusic() {
        if (mediaPlayer != null && isPlayMusic) {
            mediaPlayer?.pause()
            isPlayMusic = false
            startForceground(this, updateSong as Song, isPlayMusic)
        }
   }

   override fun onDestroy() {
        super.onDestroy()
        this.stopForeground(true)
        cancelNotification()
        mediaPlayer?.stop()
        stopSelf()
   }

   private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = getString(R.string.msg_remind)
            }
            val notificationManager = this.getSystemService(
                    NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
   }

   private fun cancelNotification() {
        val notificationManager = ContextCompat.getSystemService(
                this, NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelAll()
   }

   class SongBinder(private val service: SongService) : Binder() {
        fun getService() = service
   }

   fun getStatusPlayMusic(): Boolean {
        return isPlayMusic
   }

   fun getSong(): Song? {
       return updateSong
   }

   fun getIntentService(context: Context) = Intent(context, SongService::class.java)

   companion object {
        const val ACTION_PAUSE = 1
        const val ACTION_RESUME = 2
        const val ACTION_CANCEL = 3
        const val ACTION_PREVIOUS = 5
        const val ACTION_NEXT = 6
        const val NOTIFICATION_MUSIC_ID = 1
    }

}
