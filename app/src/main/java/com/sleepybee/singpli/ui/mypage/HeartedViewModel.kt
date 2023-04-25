package com.sleepybee.singpli.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.sleepybee.singpli.database.YTSnippetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HeartedViewModel @Inject constructor(
    ytSnippetRepository: YTSnippetRepository
) : ViewModel() {

    val heartedSnippets = ytSnippetRepository.getHeartedSnippets().asLiveData()

}