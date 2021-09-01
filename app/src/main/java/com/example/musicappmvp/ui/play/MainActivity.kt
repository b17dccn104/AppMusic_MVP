package com.example.musicappmvp.ui.play

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.musicappmvp.R
import com.example.musicappmvp.model.Song
import com.example.musicappmvp.service.SongService
import com.example.musicappmvp.ui.adapter.SongAdapter
import com.example.musicappmvp.utils.Repository
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SongInterface.View {
    private var songPresenter: SongPresenter? = null
    private var songService: SongService? = null
    private var musicBound = false
    private var listSong = mutableListOf<Song>()
    private var adapter = SongAdapter(clickItem = { index -> clickSong(index) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        if (checkPermission()) {
            loadSong()
        } else requestPermission()
    }

    fun stopForceGroundService(){
        var intent = Intent(this, SongService::class.java)
        stopService(intent)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            )
                return false
        }
        return true
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 111
            )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            loadSong()
        }
    }

    private fun loadSong() {
        songPresenter = SongPresenter(this, Repository.getSongRepository(contentResolver))
        songPresenter?.getSongFromLocal()
        recycler_listsong.adapter = adapter
    }

    override fun updateAdapter(Songs: List<Song>) {
        listSong = Songs as MutableList<Song>
        adapter.updateData(Songs)
    }

    override fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    var musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as SongService.SongBinder
            songService = binder.getService()
            musicBound = true
            handleMusic()
            setStatusButtonControl()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }

    }

    private fun handleMusic() {
        linearlayout_bottom.visibility = View.VISIBLE
        showSongToLayoutBottom()
        setStatusButtonControl()
    }

    fun showSongToLayoutBottom() {
        textview_title_song_home.text = songService?.getSong()?.Title
        textview_author_song_home.text = songService?.getSong()?.Author
        button_prev_home.setImageResource(R.drawable.ic_previous)
        button_next_home.setImageResource(R.drawable.ic_next)
        setOnClickPauseOrPlayLayoutBottom()
        setOnClickCancelLayoutBottom()
    }

    fun setOnClickPauseOrPlayLayoutBottom() {
        button_pause_home.setOnClickListener {
            if (songService?.getStatusPlayMusic() == true) {
                songService?.pauseMusic()
                setStatusButtonControl()
            } else {
                songService?.resumeMusic()
                setStatusButtonControl()
            }
        }
    }

    fun setStatusButtonControl() {
        if (songService?.getStatusPlayMusic() == true) {
            button_pause_home.setImageResource(R.drawable.ic_pause)
        } else {
            button_pause_home.setImageResource(R.drawable.ic_play)
        }
    }

    fun setOnClickCancelLayoutBottom() {
        button_cancel_home.setOnClickListener {
            songService?.cancelMusic()
            unbindService(musicConnection)
            linearlayout_bottom.visibility = View.GONE
            musicBound = false
        }
    }

    private fun clickSong(position: Int) {
        if (songService?.isPlaying() == true) {
            songService?.cancelMusic()
            unbindService(musicConnection)
        }
        stopForceGroundService()
        var intent = Intent(this,SongService::class.java)
        var bundle = Bundle()
        bundle.putParcelable(getString(R.string.action_intent_listsong), listSong[position])
        intent.putExtras(bundle)
        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicConnection)
    }

}
