package com.sleepybee.singpli.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.ItemSongListBinding
import com.sleepybee.singpli.item.KaraokeItem
import com.sleepybee.singpli.item.SongItem

/**
 * Created by leeseulbee on 2022/07/26.
 */
class SongListAdapter : RecyclerView.Adapter<SongListViewHolder>() {

    private var songList = mutableListOf<SongItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
        val binding =
            ItemSongListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongListViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: SongListViewHolder, position: Int) {
        val isFooter = position == songList.size
        if (isFooter) {
            holder.bindFooter()
        } else {
            holder.bind(
                songList[position],
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return songList.size + 1
    }

    fun addSong(songItem: SongItem) {
        this.songList.add(songItem)
        notifyItemInserted(this.songList.size-1)
    }

    fun setSongList(songList: ArrayList<SongItem>) {
        this.songList.clear()
        this.songList = songList
        notifyDataSetChanged()
    }
}

class SongListViewHolder(private val binding: ItemSongListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bindFooter() {
        binding.cardItemSongList.visibility = View.GONE
        binding.tvInfoItemSongList.visibility = View.VISIBLE
    }

    fun bind(songItem: SongItem, position: Int) {
        binding.cardItemSongList.visibility = View.VISIBLE
        binding.tvInfoItemSongList.visibility = View.GONE

        binding.tvIndexItemSongList.text = (position + 1).toString()
        binding.tvTitleItemSongList.text = "${songItem.title} - ${songItem.artist}"
        binding.llItemSongList.removeAllViews()

        var karaokeList = Gson().fromJson(songItem.karaokeList, Array<KaraokeItem>::class.java).toList()

        for (karaokeItem in karaokeList) {
            with(karaokeItem) {
                val karaokeItemView =
                    LayoutInflater.from(binding.root.context).inflate(R.layout.item_karaoke, null)
                val ivKaraokeBrand =
                    karaokeItemView.findViewById<ImageView>(R.id.iv_brand_item_karaoke)
                val tvKaraokeTitle =
                    karaokeItemView.findViewById<TextView>(R.id.tv_title_item_karaoke)
                val tvKaraokeArtist =
                    karaokeItemView.findViewById<TextView>(R.id.tv_artist_item_karaoke)
                val tvKaraokeNumber =
                    karaokeItemView.findViewById<TextView>(R.id.tv_number_item_karaoke)

                tvKaraokeTitle.text = title
                tvKaraokeArtist.text = artist
                tvKaraokeNumber.text = number.toString()

                if (brand == "TJ") {
                    ivKaraokeBrand.setBackgroundResource(R.drawable.ic_tj)
                } else if (brand == "금영") {
                    ivKaraokeBrand.setBackgroundResource(R.drawable.ic_ky)
                }

                binding.llItemSongList.addView(karaokeItemView)
            }

        }
    }
}