package com.sleepybee.singpli.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.gson.JsonObject
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.FragmentSearchBinding
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.network.YTService
import com.sleepybee.singpli.ui.adapter.RecentListAdapter
import com.sleepybee.singpli.ui.adapter.SnippetListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val snippetListAdapter = SnippetListAdapter()

    private var searchViewModel: SearchViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        searchViewModel  =
            ViewModelProvider(this)[SearchViewModel::class.java]

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.rvSearch.adapter = snippetListAdapter

        binding.btnDeleteSearch.setOnClickListener {
            clearKeyword()
        }

        binding.etSearch.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KEYCODE_ENTER) {
                hideKeyboard(v)
                searchVideo(binding.etSearch.text.toString())
            }
            true
        }
        binding.btnSearch.setOnClickListener {
            hideKeyboard(it)
            searchVideo(binding.etSearch.text.toString())
        }


        requireActivity().onBackPressedDispatcher.addCallback {
            if (binding.rvSearch.visibility == View.VISIBLE) {
                clearKeyword()
            } else {
                requireActivity().onBackPressed()
            }
        }
        return root
    }

    private fun clearKeyword() {
        binding.etSearch.setText("")
        binding.btnDeleteSearch.visibility = View.GONE
        binding.rvSearch.visibility = View.GONE
        binding.rvSearch.removeAllViewsInLayout()
        binding.includeEmptySearch.root.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel?.getRecentSnippets()?.observe(viewLifecycleOwner, Observer {

            if (it.isEmpty()) {
                binding.includeEmptySearch.tvRecentSearchEmpty.visibility = View.GONE
            } else {
                binding.includeEmptySearch.tvRecentSearchEmpty.visibility = View.VISIBLE
                val recentListAdapter = RecentListAdapter()
                Timber.d("recent : %s", it.toString())
                recentListAdapter.setSnippetList(it)
                binding.includeEmptySearch.rvRecentSearchEmpty.adapter = recentListAdapter
            }
        })

        val chipGroup = binding.includeEmptySearch.chipGroupSearchEmpty
        val inflater = LayoutInflater.from(chipGroup.context)

        searchViewModel?.getRecommendationKeywordList()?.map { keyword ->
            val chip = inflater.inflate(R.layout.chip_keyword, chipGroup, false) as Chip
            chip.text = keyword
            chip.setOnClickListener {
                val keywordNew = "$keyword 플리"
                binding.etSearch.setText(keywordNew)
                searchVideo(keywordNew)
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

    private fun hideKeyboard(v: View) {
        context?.let {
            val inputMethodManager = it.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun searchVideo(keyword: String) {
        if (keyword.isEmpty()) {
            return
        }
        binding.btnDeleteSearch.visibility = View.VISIBLE

        binding.includeEmptySearch.root.visibility = View.GONE
        binding.rvSearch.visibility = View.GONE
        binding.shimmerSearch.visibility = View.VISIBLE
        binding.shimmerSearch.startShimmer()

        CoroutineScope(Dispatchers.IO).launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val ytService = retrofit.create(YTService::class.java)

            ytService.searchVideos(
                apiKey = getString(R.string.YOUTUBE_API_KEY),
                videoPart = "snippet",
                type = "video",
                maxResults = 20,
                q = keyword)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        if (response.isSuccessful) {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.shimmerSearch.stopShimmer()
                                binding.shimmerSearch.visibility = View.GONE
                                binding.rvSearch.visibility = View.VISIBLE
                            }
                            response.body()?.let {
                                parseSongMeta(it)
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    private fun parseSongMeta(responseBody: JsonObject) {
        try {
            responseBody.getAsJsonArray("items")?.let { items ->
                val snippets: ArrayList<SnippetItem> = arrayListOf()
                for (i in 0 until items.size()) {
                    items.get(i).asJsonObject?.let { item ->
                        Log.d("SB", "item : $item");
                        val videoId = item.getAsJsonObject("id")
                            .get("videoId").asString
                        val snippet = item.getAsJsonObject("snippet")
                        Log.d("sblee", "snippet : $snippet")
                        snippets.add(
                            SnippetItem(
                                videoId,
                                snippet.get("publishedAt").asString,
                                snippet.get("channelId").asString,
                                snippet.get("title").asString,
                                snippet.get("description").asString,
                                snippet.get("thumbnails").asJsonObject.get("high").asJsonObject.get("url").asString,
                                snippet.get("channelTitle").asString,
                                snippet.get("liveBroadcastContent").asString,
                                snippet.get("publishTime").asString,
                                null
                            )
                        )
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    snippetListAdapter.setSnippetList(snippets)
                }
            }
        } catch (e: Exception) {
            Timber.d("error : " +e.message)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.etSearch.setText("")
        binding.btnDeleteSearch.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}