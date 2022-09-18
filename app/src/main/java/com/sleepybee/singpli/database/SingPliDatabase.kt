package com.sleepybee.singpli.database

import android.content.Context
import androidx.room.*
import com.sleepybee.singpli.item.*
import com.sleepybee.singpli.utils.DATABASE_NAME

@Database(entities = [SnippetItem::class, SongItem::class], version = 1, exportSchema = false)
abstract class SingPliDatabase : RoomDatabase() {
    abstract fun snippetDao() : SnippetDao

    companion object {
        @Volatile private var instance: SingPliDatabase? = null

        @Synchronized
        fun getInstance(context: Context): SingPliDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context, SingPliDatabase::class.java,
                    DATABASE_NAME)
                    .build()
            }
        }

    }
}