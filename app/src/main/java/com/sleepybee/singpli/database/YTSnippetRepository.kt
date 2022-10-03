package com.sleepybee.singpli.database

import com.sleepybee.singpli.BuildConfig
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
import com.sleepybee.singpli.network.YTService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class YTSnippetRepository @Inject constructor(
    private val ytService: YTService,
    private  val snippetDao: SnippetDao
) {

//    private val recentSnippetList: MutableLiveData<List<SnippetWithSongs>>

    fun searchVideoSnippets(keyword: String, nextPageToken: String?) =
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

    fun getHeartedSnippets(): Flow<List<SnippetWithSongs>> {
        return snippetDao.getHeartedSnippets()
            .flowOn(Dispatchers.IO)
            .conflate()
    }

    fun getRecentSnippets(): Flow<List<SnippetWithSongs>> {
        return snippetDao.getRecentSnippets()
            .flowOn(Dispatchers.IO)
            .conflate()
    }

    fun updateHeart(snippetItem: SnippetItem) {
        snippetDao.updateHeart(snippetItem)
    }

    fun getSnippetById(videoId: String): Flow<SnippetWithSongs?> {
        return snippetDao.getSnippetById(videoId)
            .flowOn(Dispatchers.IO)
            .conflate()
    }

    fun deleteSnippetById(videoId: String) {
        return snippetDao.deleteSongsById(videoId)
    }
}