package com.sleepybee.singpli.ui.search

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.gson.JsonObject
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.FragmentSearchBinding
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.ui.adapter.RecentListAdapter
import com.sleepybee.singpli.ui.adapter.SnippetListAdapter
import com.sleepybee.singpli.ui.common.BaseDataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : BaseDataBindingFragment<FragmentSearchBinding>(R.layout.fragment_search) {

    private val snippetListAdapter = SnippetListAdapter()
    private val recentListAdapter = RecentListAdapter()

    private val searchViewModel: SearchViewModel by viewModels()
    private var keyword = ""
    private var nextPageToken: String? = null
    private var isNew = true
    private var isLoading = true

    override fun initBinding() {

        binding.rvSearch.adapter = snippetListAdapter
        binding.includeEmptySearch.rvRecentSearchEmpty.adapter = recentListAdapter

        binding.btnDeleteSearch.visibility = if (keyword.isEmpty()) View.GONE else View.VISIBLE
        binding.btnDeleteSearch.setOnClickListener {
            clearKeyword()
        }

        binding.etSearch.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KEYCODE_ENTER) {
                hideKeyboard(v)
                searchVideo(true, binding.etSearch.text.toString())
            }
            true
        }
        binding.btnSearch.setOnClickListener {
            hideKeyboard(it)
            keyword = binding.etSearch.text.toString()
            searchVideo(true, keyword)
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            if (binding.rvSearch.visibility == View.VISIBLE) {
                clearKeyword()
            } else {
                activity?.onBackPressed()
            }
        }

    }

    private fun clearKeyword() {
        keyword = ""
        nextPageToken = null
        binding.etSearch.setText("")
        binding.btnDeleteSearch.visibility = View.GONE
        binding.includeEmptySearch.root.visibility = View.VISIBLE
        searchViewModel.clearSearchSnippets()
        binding.rvSearch.removeAllViewsInLayout()
        binding.rvSearch.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel.recentSnippets.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.includeEmptySearch.tvRecentSearchEmpty.visibility = View.GONE
            } else {
                binding.includeEmptySearch.tvRecentSearchEmpty.visibility = View.VISIBLE
                Timber.d("recent : %s", it.toString())
                recentListAdapter.setSnippetList(it)
            }
        }

        val chipGroup = binding.includeEmptySearch.chipGroupSearchEmpty
        val inflater = LayoutInflater.from(chipGroup.context)

        searchViewModel.getRecommendationKeywordList().observe(viewLifecycleOwner) {
            it.map { chipKeyword ->
                val chip = inflater.inflate(R.layout.chip_keyword, chipGroup, false) as Chip
                chip.text = chipKeyword
                chip.setOnClickListener {
                    val keywordNew = "$chipKeyword 플리"
                    binding.etSearch.setText(keywordNew)
                    searchVideo(true, keywordNew)
                    hideKeyboard(view)
                }
                chip
            }?.let {
                chipGroup.removeAllViews()

                for (chip in it) {
                    chipGroup.addView(chip)
                }
            }
        }

        searchViewModel.getSearchSnippetJsonObject().observe(viewLifecycleOwner) {
            binding.shimmerSearch.stopShimmer()
            binding.shimmerSearch.visibility = View.GONE
            if (it == null) {
                binding.includeEmptySearch.root.visibility = View.VISIBLE
                binding.rvSearch.visibility = View.GONE
            } else {
                binding.includeEmptySearch.root.visibility = View.GONE
                binding.rvSearch.visibility = View.VISIBLE
                parseSongMeta(it)
            }
        }
    }

    private fun hideKeyboard(v: View) {
        context?.let {
            val inputMethodManager = it.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun searchVideo(isNew: Boolean, keyword: String) {
        if (keyword.isEmpty()) {
            return
        }
        this.isNew = isNew
        this.keyword = keyword
        binding.btnDeleteSearch.visibility = View.VISIBLE
        binding.includeEmptySearch.root.visibility = View.GONE
        if (isNew) {
            binding.shimmerSearch.visibility = View.VISIBLE
            binding.shimmerSearch.startShimmer()
        }

        searchViewModel.searchVideoSnippets(keyword, nextPageToken)
    }

    private fun parseSongMeta(responseBody: JsonObject) {
        try {
            nextPageToken = responseBody.get("nextPageToken").asString
            responseBody.getAsJsonArray("items")?.let { items ->
                val snippets: ArrayList<SnippetItem> = arrayListOf()
                for (i in 0 until items.size()) {
                    items.get(i).asJsonObject?.let { item ->
                        val videoId = item.getAsJsonObject("id")
                            .get("videoId").asString
                        val snippet = item.getAsJsonObject("snippet")
                        snippets.add(
                            SnippetItem(
                                videoId,
                                snippet.get("publishedAt").asString,
                                snippet.get("channelId").asString,
                                snippet.get("title").asString,
                                snippet.get("description").asString,
                                snippet.get("thumbnails").asJsonObject.get("high").asJsonObject.get(
                                    "url"
                                ).asString,
                                snippet.get("channelTitle").asString,
                                snippet.get("liveBroadcastContent").asString,
                                snippet.get("publishTime").asString,
                                null
                            )
                        )
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    isLoading = false
                    snippetListAdapter.addSnippets(isNew, snippets)
                }
            }
        } catch (e: Exception) {
            Timber.d("error : %s", e.message)
        }
    }

}