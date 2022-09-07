package com.sleepybee.singpli.network

import com.google.gson.JsonObject
import com.squareup.okhttp.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

/**
 * Created by leeseulbee on 2022/07/28.
 */
interface YTService {

    @GET("youtube/v3/search")
    fun searchVideos(
        @Query("key") apiKey: String,
        @Query("part") videoPart: String,
        @Query("type") type: String,
        @Query("maxResults") maxResults: Int,
        @Query("q") q: String,
        @Query("pageToken") pageToken: String? = null
    ): Call<JsonObject>
}