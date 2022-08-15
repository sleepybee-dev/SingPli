package com.sleepybee.singpli.database

import android.app.Application
import androidx.lifecycle.LiveData
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem

class SnippetRepository(application: Application) {
    private val snippetDao: SnippetDao
    private val recentSnippetList: LiveData<List<SnippetWithSongs>>

    init {
        var db = SingPliDatabase.getInstance(application)
        snippetDao = db!!.snippetDao()
        recentSnippetList = db.snippetDao().getRecentSnippets()
    }

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