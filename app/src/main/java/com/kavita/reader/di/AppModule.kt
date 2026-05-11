package com.kavita.reader.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.kavita.reader.data.AppDatabase
import com.kavita.reader.data.KavitaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "kavita_reader.db")
            .build()

    @Provides
    fun provideDao(database: AppDatabase): KavitaDao = database.kavitaDao()

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
