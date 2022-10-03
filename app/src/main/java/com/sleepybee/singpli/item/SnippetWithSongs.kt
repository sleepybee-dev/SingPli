package com.sleepybee.singpli.item

import androidx.room.Embedded
import androidx.room.Relation
import java.io.Serializable

data class SnippetWithSongs(
    @Embedded var snippet: SnippetItem,
    @Relation(
        parentColumn = "videoId",
        entityColumn = "videoId",
        entity = SongItem::class
    )
    var songList: List<SongItem>? = null
): Serializable {
    constructor(): this(SnippetItem(), listOf<SongItem>())
}