package com.sleepybee.singpli.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.ItemSearchListBinding
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.ui.adapter.SearchListViewHolder.Companion.diffUtil
import com.sleepybee.singpli.ui.custom.CropTransformation
import com.sleepybee.singpli.ui.search.SongListActivity


/**
 * Created by leeseulbee on 2022/08/01.
 */
class SnippetListAdapter : ListAdapter<SnippetItem, SearchListViewHolder>(diffUtil) {

    private var fullSnippetList = mutableListOf<SnippetItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        val binding =
            ItemSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun addSnippets(isNew: Boolean, snippetList: ArrayList<SnippetItem>) {
        if (isNew) fullSnippetList.clear()
        fullSnippetList.addAll(snippetList)
        submitList(fullSnippetList)
    }
}

class SearchListViewHolder(private val binding: ItemSearchListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(snippetItem: SnippetItem) {
        with(snippetItem) {
            binding.tvTitleItemSearchList.text = title
            binding.tvChannelDateItemSearchList.text =
                "$channelTitle | ${publishedAt.split("T")[0]}"

            val multiOption = MultiTransformation(
                CropTransformation(),
                CenterCrop(),
                RoundedCorners(20),
            )

            Glide.with(binding.root.context)
                .load(thumbnail)
                .apply(RequestOptions().transform(multiOption))
                .dontAnimate()
                .placeholder(R.drawable.ic_baseline_image)
                .into(binding.ivThumbnailItemSearchList)


            // https://www.youtube.com/watch?v=VoMZ8G1arRg

            val videoUrl = "https://www.youtube.com/watch?v=${videoId}"

            binding.btnBrowseItemSearchList.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                binding.root.context.startActivity(browserIntent)
            }

            binding.clTitleSearchList.setOnClickListener {
                goSongListActivity(binding.root.context, snippetItem)
            }
        }
    }

    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<SnippetItem>(){
            override fun areItemsTheSame(oldItem: SnippetItem, newItem: SnippetItem): Boolean {
                return oldItem.videoId == newItem.videoId
            }

            override fun areContentsTheSame(oldItem: SnippetItem, newItem: SnippetItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}

private fun goSongListActivity(context: Context, snippetItem: SnippetItem) {
    val intent = Intent(context, SongListActivity::class.java)
    intent.putExtra("snippet", snippetItem)
    context.startActivity(intent)
}


