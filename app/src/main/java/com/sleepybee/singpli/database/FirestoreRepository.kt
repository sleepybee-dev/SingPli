package com.sleepybee.singpli.database

import android.app.Application
import com.sleepybee.singpli.item.SnippetWithSongs
import kotlinx.coroutines.flow.Flow

class FirestoreRepository(application: Application) {
    private val snippetDao: SnippetDao
    private val recentSnippetList: Flow<List<SnippetWithSongs>>

    init {
        val db = SingPliDatabase.getInstance(application)
        snippetDao = db.snippetDao()
        recentSnippetList = db.snippetDao().getRecentSnippets()
    }

}