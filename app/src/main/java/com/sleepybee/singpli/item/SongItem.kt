package com.sleepybee.singpli.item

import androidx.room.*
import java.io.Serializable

/**
 * Created by leeseulbee on 2022/07/26.
 */
@Entity(tableName = "song")
data class SongItem (
    var videoId: String = "",
    @PrimaryKey(autoGenerate = true)
    var songId : Int? = null,
    var title: String = "",
    var artist: String = "",
    var karaokeList: String = ""
) : Serializable {
    constructor(): this("", 0, "", "", )
}