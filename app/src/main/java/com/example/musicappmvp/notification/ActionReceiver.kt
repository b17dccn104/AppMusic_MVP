package com.example.musicappmvp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicappmvp.R
import com.example.musicappmvp.service.SongService

class ActionReceiver : BroadcastReceiver() {
    private val songService: SongService? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        var actionMusic =
                intent?.getIntExtra(context?.getString(R.string.action_intent_music_service), 0)
        var intentReceiver = context?.let { songService?.getIntentService(it) }
        intentReceiver?.putExtra(context?.getString(R.string.action_intent_music_receiver), actionMusic)
        context?.startService(intentReceiver)
    }
}
