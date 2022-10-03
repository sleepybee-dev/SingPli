package com.sleepybee.singpli.item

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