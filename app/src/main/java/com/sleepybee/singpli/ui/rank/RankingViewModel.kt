package com.sleepybee.singpli.ui.rank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RankingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "We're preparing"
    }
    val text: LiveData<String> = _text
}