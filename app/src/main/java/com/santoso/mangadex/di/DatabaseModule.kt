package com.santoso.mangadex.di

import android.content.Context
import androidx.room.Room
import com.santoso.mangadex.data.local.MangaDao
import com.santoso.mangadex.data.local.MangaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MangaDatabase {
        return Room.databaseBuilder(
            context,
            MangaDatabase::class.java,
            "mangadex_db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    @Provides
    @Singleton
    fun provideMangaDao(database: MangaDatabase): MangaDao {
        return database.mangaDao()
    }
}
