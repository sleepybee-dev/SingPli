package com.sleepybee.singpli.ui.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.TypedValue
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
import com.sleepybee.singpli.databinding.ItemRecentBinding
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.ui.custom.CropTransformation
import com.sleepybee.singpli.ui.search.SongListActivity
import timber.log.Timber
import java.net.URL

class RecentListAdapter : RecyclerView.Adapter<RecentListViewHolder>() {

    private var snippetList = listOf<SnippetWithSongs>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentListViewHolder {
        val binding =
            ItemRecentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentListViewHolder, position: Int) {
        holder.bind(snippetList[position], position)
    }

    override fun getItemCount(): Int {
        return snippetList.size
    }

    fun setSnippetList(snippetList: List<SnippetWithSongs>) {
        this.snippetList = snippetList
        notifyDataSetChanged()
    }
}

class RecentListViewHolder(private val binding: ItemRecentBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(snippetWithSongs: SnippetWithSongs, position: Int) {
        with(snippetWithSongs) {
            if (position == 0) {
                val itemParam = binding.llItemRecent.layoutParams as ViewGroup.MarginLayoutParams
                itemParam.setMargins(dpFromPx(20), dpFromPx(20),dpFromPx(20), dpFromPx(8))
                binding.llItemRecent.layoutParams = itemParam
            }
            binding.tvTitleItemRecent.text = snippet.title

            val multiOption = MultiTransformation(
                CropTransformation(),
                CenterCrop(),
                RoundedCorners(40),
            )

            Glide.with(binding.root.context)
                .load(snippet.thumbnail)
                .apply(RequestOptions().transform(multiOption))
                .dontAnimate()
                .placeholder(R.drawable.ic_baseline_image)
                .into(binding.ivItemRecent)

            binding.llItemRecent.setOnClickListener {
                goSongListActivity(binding.root.context, snippetWithSongs)
            }
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

    private fun dpFromPx(dp : Int) : Int {
        var displayMetrics = binding.root.context.resources.displayMetrics
        val pixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics)
        Log.d("SB", "px dp : $pixel / $dp")
        return pixel.toInt()
    }

    private fun goSongListActivity(context: Context, snippetWithSongs: SnippetWithSongs) {
        val intent = Intent(context, SongListActivity::class.java)
        with(snippetWithSongs) {
            intent.putExtra("snippet", snippet)
            intent.putExtra("songList", Gson().toJson(songList))
            context.startActivity(intent)
        }
    }
}
