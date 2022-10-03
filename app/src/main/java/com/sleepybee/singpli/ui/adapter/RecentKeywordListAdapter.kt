package com.sleepybee.singpli.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sleepybee.singpli.databinding.ItemRecentKeywordBinding

class RecentKeywordListAdapter : RecyclerView.Adapter<RecentKeywordListViewHolder>() {

    private var keywordList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentKeywordListViewHolder {
        val binding =
            ItemRecentKeywordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentKeywordListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentKeywordListViewHolder, position: Int) {
        holder.bind(keywordList[position])
    }

    override fun getItemCount(): Int {
        return keywordList.size
    }

    fun setKeywordList(keywordList: ArrayList<String>) {
        this.keywordList = keywordList
        notifyDataSetChanged()
    }
}

class RecentKeywordListViewHolder(private val binding: ItemRecentKeywordBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(keyword: String) {
        with(keyword) {
            binding.tvKeywordItemRecentKeyword.text = keyword

            binding.btnRemoveItemRecentKeyword.setOnClickListener {
                removeKeyword(keyword)
            }
        }
    }

    private fun removeKeyword(keyword: String) {

    }
}
