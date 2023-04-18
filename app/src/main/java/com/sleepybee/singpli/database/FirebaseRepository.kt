package com.sleepybee.singpli.database

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.sleepybee.singpli.item.SnippetWithSongs
import kotlinx.coroutines.flow.Flow

class FirebaseRepository {
//    private val snippetDao: SnippetDao
//    private val recentSnippetList: Flow<List<SnippetWithSongs>>

    private val remoteConfigInstance: FirebaseRemoteConfig = Firebase.remoteConfig
    val keywordList = MutableLiveData<List<String>>()

    init {

        remoteConfigInstance.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3000
            }
        )
        remoteConfigInstance.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    keywordList.value = remoteConfigInstance.getString("keyword").split(",")
                }
            }
//        val db = SingPliDatabase.getInstance(application)
//        snippetDao = db.snippetDao()
//        recentSnippetList = db.snippetDao().getRecentSnippets()
    }

}