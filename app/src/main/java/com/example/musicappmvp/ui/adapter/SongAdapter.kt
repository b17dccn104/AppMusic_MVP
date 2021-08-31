package com.example.musicappmvp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicappmvp.R
import com.example.musicappmvp.model.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongAdapter(private val clickItem: ((Int) -> Unit)) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private var listSong = mutableListOf<Song>()

    fun updateData(songsList: List<Song>) {
        listSong.clear()
        listSong.addAll(songsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(itemView, clickItem)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bindData(listSong[position])
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    class SongViewHolder(
            itemView : View,
            private var itemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }

        fun bindData(song: Song) {
            itemView.apply {
                textview_title.text = song.Title
                textview_author.text = song.Author
            }
        }

    }

}
