package com.sleepybee.singpli.database

import androidx.lifecycle.LiveData
import com.sleepybee.singpli.BuildConfig
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
import com.sleepybee.singpli.network.YTService
import javax.inject.Inject

class YTSnippetRepository @Inject constructor(
    private val ytService: YTService,
    private  val snippetDao: SnippetDao
) {

//    private val recentSnippetList: MutableLiveData<List<SnippetWithSongs>>

    suspend fun searchVideoSnippets(keyword: String, nextPageToken: String?) =
        ytService.searchVideos(
            apiKey = BuildConfig.YOUTUBE_API_KEY,
            videoPart = "snippet",
            type = "video",
            maxResults = 40,
            q = keyword,
            pageToken = nextPageToken)

    fun insertRecentSnippet(snippetItem: SnippetItem) {
        snippetDao.insertRecentSnippets(snippetItem)
    }

    fun insertSongs(songList: List<SongItem>) {
        snippetDao.insertSongs(songList)
    }

    fun getHeartedSnippets(): LiveData<List<SnippetWithSongs>>? {
        return snippetDao.getHeartedSnippets()
    }

    fun getRecentSnippets(): LiveData<List<SnippetWithSongs>>? {
        try {
            return snippetDao.getRecentSnippets()
        } catch (e: Exception) {

        }
        return null
    }

    fun updateHeart(snippetItem: SnippetItem) {
        snippetDao.updateHeart(snippetItem)
    }
}