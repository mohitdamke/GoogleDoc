package com.example.googledoc.domain

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.googledoc.data.DocumentEntity

@Database(entities = [DocumentEntity::class], version = 1)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

}
