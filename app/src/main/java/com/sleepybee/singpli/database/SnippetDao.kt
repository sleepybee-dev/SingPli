package com.sleepybee.singpli.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
//WHERE viewDate IS NOT NULL ORDER BY viewDate
@Dao
interface SnippetDao {
    @Transaction
    @Query("SELECT * FROM snippet WHERE viewDate IS NOT NULL ORDER BY viewDate DESC LIMIT 10")
    fun getRecentSnippets() : LiveData<List<SnippetWithSongs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentSnippets(snippetItem: SnippetItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongs(songList: List<SongItem>)

    @Update
    fun updateHeart(snippetItem: SnippetItem)

    @Transaction
    @Query("SELECT * FROM snippet WHERE isHearted == 1")
    fun getHeartedSnippets(): LiveData<List<SnippetWithSongs>>?

}