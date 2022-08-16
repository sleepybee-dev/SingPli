package com.sleepybee.singpli.ui.search

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.sleepybee.singpli.database.SnippetRepository
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SnippetRepository(application)

    private val recentSnippets = repository.getRecentSnippets()

    private val recommendationKeyword =
        listOf("노래방", "여름", "비 오는 날", "청량", "걸그룹", "감성힙합", "뮤지컬 영화", "애니")

    private var searchSnippetJsonObject = MutableLiveData<JsonObject?>()
    private val ioDispatcher = Dispatchers.IO

    fun insertRecentSnippet(snippetItem: SnippetItem) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.insertRecentSnippet(snippetItem)
            }
        }
    }

    fun insertSongs(songList: List<SongItem>) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.insertSongs(songList)
            }
        }
    }

    fun updateHeart(snippetItem: SnippetItem) {
        repository.updateHeart(snippetItem)
    }

    fun searchVideoSnippets(keyword: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    val response = repository.searchVideoSnippets(keyword).execute()
                    if (response.isSuccessful) {
                        searchSnippetJsonObject.postValue(response.body())
                    }
                } catch (e: Exception) {
                    Timber.d(e)
                }
            }
        }
    }
    fun getSearchSnippetJsonObject(): LiveData<JsonObject?> {
        return searchSnippetJsonObject
    }
    fun getRecentSnippets(): LiveData<List<SnippetWithSongs>>? {
        return recentSnippets
    }

    fun getRecommendationKeywordList(): List<String> {
        return recommendationKeyword
    }

    fun clearSearchSnippets() {
        searchSnippetJsonObject.postValue(null)
    }

//    fun getKeywords(): LiveData<List<String>> {
//        FirebaseFirestore.getInstance().collection("keyword").orderBy("countSearch")
//            .limit(10).get()
//
//    }

}