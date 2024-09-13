package com.example.googledoc.domain

import android.content.Context
import androidx.room.Room
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
    fun provideDocumentDao(database: DocumentDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    @Singleton
    fun provideDocumentDatabase(@ApplicationContext appContext: Context): DocumentDatabase {
        return Room.databaseBuilder(
            appContext,
            DocumentDatabase::class.java,
            "document_database"
        ).build()
    }
}
