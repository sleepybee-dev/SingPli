package com.sleepybee.singpli.ui.starred

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sleepybee.singpli.database.YTSnippetRepository
import com.sleepybee.singpli.item.SnippetWithSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HeartedViewModel @Inject constructor(
    private val ytSnippetRepository: YTSnippetRepository
) : ViewModel() {

    private val recentSnippets = ytSnippetRepository.getRecentSnippets()
    private val heartedSnippets = ytSnippetRepository.getHeartedSnippets()

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