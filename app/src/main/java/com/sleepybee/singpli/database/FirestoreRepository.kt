package com.sleepybee.singpli.database

import android.app.Application
import androidx.lifecycle.LiveData
import com.sleepybee.singpli.item.SnippetWithSongs

class FirestoreRepository(application: Application) {
    private val snippetDao: SnippetDao
    private val recentSnippetList: LiveData<List<SnippetWithSongs>>

    init {
        var db = SingPliDatabase.getInstance(application)
        snippetDao = db!!.snippetDao()
        recentSnippetList = db.snippetDao().getRecentSnippets()
    }

}