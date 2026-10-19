package com.santoso.mangadex.di

import com.santoso.mangadex.data.local.MangaDao
import com.santoso.mangadex.data.remote.MangaDexApi
import com.santoso.mangadex.data.repository.MangaRepository
import com.santoso.mangadex.ui.viewmodel.ThemeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMangaRepository(
        api: MangaDexApi,
        dao: MangaDao
    ): MangaRepository {
        return MangaRepository(api, dao)
    }

    @Provides
    @Singleton
    fun provideThemeViewModel(): ThemeViewModel {
        return ThemeViewModel()
    }
}
