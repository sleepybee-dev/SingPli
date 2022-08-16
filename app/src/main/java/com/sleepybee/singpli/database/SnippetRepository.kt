package com.sleepybee.singpli.database

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.sleepybee.singpli.R
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
import com.sleepybee.singpli.network.YTService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SnippetRepository(application: Application) {
    private val snippetDao: SnippetDao
    private var ytApiKey = ""
    private val recentSnippetList: LiveData<List<SnippetWithSongs>>

    private val ytService = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(YTService::class.java)

    init {
        var db = SingPliDatabase.getInstance(application)
        snippetDao = db!!.snippetDao()
        recentSnippetList = db.snippetDao().getRecentSnippets()
        ytApiKey = application.getString(R.string.YOUTUBE_API_KEY)
    }

    suspend fun searchVideoSnippets(keyword: String) =
        ytService.searchVideos(
            apiKey = ytApiKey,
            videoPart = "snippet",
            type = "video",
            maxResults = 20,
            q = keyword)

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