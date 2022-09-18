package com.sleepybee.singpli.ui.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.ItemSearchListBinding
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.ui.custom.CropTransformation
import com.sleepybee.singpli.ui.search.SongListActivity
import com.sleepybee.singpli.utils.INTENT_KEY_SNIPPET
import com.sleepybee.singpli.utils.INTENT_KEY_SONG_LIST
import timber.log.Timber
import java.net.URL


/**
 * Created by leeseulbee on 2022/08/01.
 */
class HeartedListAdapter : RecyclerView.Adapter<HeartedListViewHolder>() {

    private var snippetList = mutableListOf<SnippetWithSongs>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeartedListViewHolder {
        val binding =
            ItemSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HeartedListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeartedListViewHolder, position: Int) {
        holder.bind(snippetList[position])
    }

    override fun getItemCount(): Int {
        return snippetList.size
    }

    fun setSnippetList(snippetList: ArrayList<SnippetWithSongs>) {
        this.snippetList = snippetList
        notifyDataSetChanged()
    }
}

class HeartedListViewHolder(private val binding: ItemSearchListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(snippetItem: SnippetWithSongs) {
        with(snippetItem) {
            binding.tvTitleItemSearchList.text = snippet.title
            binding.tvChannelDateItemSearchList.text = "${snippet.channelTitle} | ${snippet.publishedAt.split("T")[0]}"

            val multiOption = MultiTransformation(
                CropTransformation(),
                CenterCrop(),
                RoundedCorners(20),
            )

            Glide.with(binding.root.context)
                .load(snippet.thumbnail)
                .apply(RequestOptions().transform(multiOption))
                .dontAnimate()
                .placeholder(R.drawable.ic_baseline_image)
                .into(binding.ivThumbnailItemSearchList)

            // https://www.youtube.com/watch?v=VoMZ8G1arRg

            val videoUrl = "https://www.youtube.com/watch?v=${snippet.videoId}"

            binding.btnBrowseItemSearchList.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                binding.root.context.startActivity(browserIntent)
            }
        }

        binding.clTitleSearchList.setOnClickListener {
            goSongListActivity(binding.root.context, snippetItem)
        }

    }


    private fun goSongListActivity(context: Context, snippetWithSongs: SnippetWithSongs) {
        val intent = Intent(context, SongListActivity::class.java)
        with(snippetWithSongs) {
            intent.putExtra(INTENT_KEY_SNIPPET, snippet)
            intent.putExtra(INTENT_KEY_SONG_LIST, Gson().toJson(songList))
            context.startActivity(intent)
        }
    }

    private fun generateScaledBitmap(thumbnail: String): Bitmap? {
        val bitmap: Bitmap;
        try {
            val thumbnailUrl = URL(thumbnail)
            val connection = thumbnailUrl.openConnection()
            connection.doInput = true
            connection.connect()

            val inputStream = connection.getInputStream()
            bitmap = BitmapFactory.decodeStream(inputStream)

            return Bitmap.createBitmap(bitmap, 0, 45, bitmap.width, bitmap.width * 9 / 16)
        } catch (e: Exception) {
            Timber.d(e)
        }
        return null
    }
}
