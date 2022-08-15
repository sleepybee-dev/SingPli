package com.sleepybee.singpli.item

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by leeseulbee on 2022/07/26.
 */
data class KaraokeItem (
    var brand: String = "",
    var number: Int = 0,
    var title: String = "",
    var artist: String = ""
) : Serializable {
    constructor(): this("", 0, "", "")
}