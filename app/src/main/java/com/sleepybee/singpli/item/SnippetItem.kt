package com.sleepybee.singpli.item

import androidx.room.*
import java.io.Serializable

/**
 * Created by leeseulbee on 2022/08/01.
 */

@Entity(tableName = "snippet")
data class SnippetItem(
    @PrimaryKey
    var videoId: String = "",
    var publishedAt: String = "",
    var channelId: String = "",
    var title: String = "",
    var description: String = "",
    var thumbnail: String = "",
    var channelTitle: String = "",
    var liveBroadcastContent: String = "",
    var publishTime: String = "",
    var viewDate: String?,
    var isHearted: Boolean = false
) : Serializable {
    constructor() : this("", "", "", "", "", "", "", "","", "")
}
