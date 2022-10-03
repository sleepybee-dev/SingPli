package com.sleepybee.singpli.database

import androidx.room.*
import com.sleepybee.singpli.item.SnippetItem
import com.sleepybee.singpli.item.SnippetWithSongs
import com.sleepybee.singpli.item.SongItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {
    @Transaction
    @Query("SELECT * FROM snippet WHERE viewDate IS NOT NULL ORDER BY viewDate DESC LIMIT 10")
    fun getRecentSnippets() : Flow<List<SnippetWithSongs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentSnippets(snippetItem: SnippetItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongs(songList: List<SongItem>)

    @Update
    fun updateHeart(snippetItem: SnippetItem)

    @Query("DELETE FROM song WHERE videoId = :videoId")
    fun deleteSongsById(videoId: String)

    @Transaction
    @Query("SELECT * FROM snippet WHERE isHearted = 1")
    fun getHeartedSnippets(): Flow<List<SnippetWithSongs>>

    @Transaction
    @Query("SELECT * FROM snippet WHERE videoId = :videoId LIMIT 1")
    fun getSnippetById(videoId: String): Flow<SnippetWithSongs?>
}