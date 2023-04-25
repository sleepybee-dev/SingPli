package com.sleepybee.singpli.ui.search

import androidx.lifecycle.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.JsonObject
import com.sleepybee.singpli.database.FirebaseRepository
import com.sleepybee.singpli.database.YTSnippetRepository
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val ytSnippetRepository: YTSnippetRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    val recentSnippets = ytSnippetRepository.getRecentSnippets().asLiveData()

    var recommendationKeyword =
        listOf("노래방", "걸그룹", "남돌", "감성힙합", "뮤지컬 영화", "애니")

    private var searchSnippetJsonObject = MutableLiveData<JsonObject?>()
    private val ioDispatcher = Dispatchers.IO

    init {
        recommendationKeyword = firebaseRepository.getKeywordList()
    }
    fun getSnippetById(videoId: String): Flow<SnippetWithSongs?> = flow {
        ytSnippetRepository.getSnippetById(videoId)
            .stateIn(
                scope = viewModelScope,
            )
    }

    fun clearSongsById(videoId: String) {
        ytSnippetRepository.deleteSnippetById(videoId)
    }

    fun insertRecentSnippet(snippetItem: SnippetItem) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                ytSnippetRepository.insertRecentSnippet(snippetItem)
            }
        }
    }

    fun clearAndInsertSongs(videoId: String, songList: List<SongItem>) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                ytSnippetRepository.deleteSnippetById(videoId)
                ytSnippetRepository.insertSongs(songList)
            }
        }
    }

    fun updateHeart(snippetItem: SnippetItem) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                ytSnippetRepository.updateHeart(snippetItem)
            }
        }
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

    fun getRecommendationKeywordList(): MutableLiveData<List<String>> {
        return firebaseRepository.keywordList
    }

    fun clearSearchSnippets() {
        searchSnippetJsonObject.postValue(null)
    }

}