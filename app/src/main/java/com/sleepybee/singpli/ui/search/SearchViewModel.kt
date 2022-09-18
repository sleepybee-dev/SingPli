package com.sleepybee.singpli.ui.search

import androidx.lifecycle.*
import com.google.gson.JsonObject
import com.sleepybee.singpli.database.YTSnippetRepository
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val ytSnippetRepository: YTSnippetRepository
) : ViewModel() {

    private val recentSnippets = ytSnippetRepository.getRecentSnippets()

    private val recommendationKeyword =
        listOf("노래방", "가을", "이별", "걸그룹", "남돌", "감성힙합", "뮤지컬 영화", "애니")

    private var searchSnippetJsonObject = MutableLiveData<JsonObject?>()
    private val ioDispatcher = Dispatchers.IO

    fun insertRecentSnippet(snippetItem: SnippetItem) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                ytSnippetRepository.insertRecentSnippet(snippetItem)
            }
        }
    }

    fun insertSongs(songList: List<SongItem>) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                ytSnippetRepository.insertSongs(songList)
            }
        }
    }

    fun updateHeart(snippetItem: SnippetItem) {
        ytSnippetRepository.updateHeart(snippetItem)
    }

    fun searchVideoSnippets(keyword: String, nextPageToken: String?) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    val response = ytSnippetRepository.searchVideoSnippets(keyword, nextPageToken).execute()
                    if (response.isSuccessful) {
                        searchSnippetJsonObject.postValue(response.body())
                    } else {
                        Timber.d(response.errorBody().toString())
                        searchSnippetJsonObject.postValue(null)
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

}