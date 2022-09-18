package com.sleepybee.singpli.ui.rank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sleepybee.singpli.database.YTSnippetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val ytSnippetRepository: YTSnippetRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "We're preparing"
    }
    val text: LiveData<String> = _text
}