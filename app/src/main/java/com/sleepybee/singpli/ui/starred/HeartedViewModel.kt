package com.sleepybee.singpli.ui.starred

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sleepybee.singpli.database.SnippetRepository
import com.sleepybee.singpli.item.SnippetWithSongs

class HeartedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SnippetRepository(application)
    private val recentSnippets = repository.getRecentSnippets()
    private val heartedSnippets = repository.getHeartedSnippets()

    private val _text = MutableLiveData<String>().apply {
        value = "We're preparing"
    }
    val text: LiveData<String> = _text

    fun getRecentSnippets(): LiveData<List<SnippetWithSongs>>? {
        return recentSnippets
    }

    fun getHeartedSnippets(): LiveData<List<SnippetWithSongs>>? {
        return heartedSnippets
    }
}