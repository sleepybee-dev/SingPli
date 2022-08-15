package com.sleepybee.singpli.database

import android.content.Context
import androidx.room.*
import com.sleepybee.singpli.item.*

@Database(entities = [SnippetItem::class, SongItem::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class SingPliDatabase : RoomDatabase() {
    abstract fun snippetDao() : SnippetDao

    companion object {
        private var instance: SingPliDatabase? = null

        @Synchronized
        fun getInstance(context: Context): SingPliDatabase? {
            if (instance == null) {
                synchronized(SingPliDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SingPliDatabase::class.java,
                        "singpli-database"
                    ).build()
                }
            }
            return instance
        }

    }
}