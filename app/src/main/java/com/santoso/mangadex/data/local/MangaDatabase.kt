package com.santoso.mangadex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class, SearchHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
}
