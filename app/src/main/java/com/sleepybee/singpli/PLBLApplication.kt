package com.sleepybee.singpli

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

/**
 * Created by leeseulbee on 2022/07/27.
 */
class PLBLApplication : Application() {
    companion object {
        var artistTransData: MutableMap<String, String> = mutableMapOf()
        var titleTransData: MutableMap<String, String> = mutableMapOf()
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        FirebaseApp.initializeApp(applicationContext)
        Timber.plant(Timber.DebugTree())
        initTranslatedData()
    }

    private fun initTranslatedData() {
        FirebaseFirestore.getInstance().collection("trans").document("artist")
            .get()
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val snapshot = it.result
                    if (snapshot.exists() && snapshot.data != null) {
                        snapshot.data!!.map { map ->
                            artistTransData.put(map.key, map.value as String)
                        }
                        Log.d("sblee", "init : $artistTransData")
                    }
                }
            }
        FirebaseFirestore.getInstance().collection("trans").document("title")
            .get()
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val snapshot = it.result
                    if (snapshot.exists() && snapshot.data != null) {
                        snapshot.data!!.map { map ->
                            titleTransData.put(map.key, map.value as String)
                        }
                        Log.d("sblee", "init : $titleTransData")
                    }
                }
            }
    }
}