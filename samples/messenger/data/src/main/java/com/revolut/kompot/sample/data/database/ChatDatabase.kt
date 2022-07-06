package com.revolut.kompot.sample.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.revolut.kompot.sample.data.database.entity.ContactEntity
import com.revolut.kompot.sample.data.database.entity.MessageEntity

@Database(
    version = 1,
    entities = [
        ContactEntity::class, MessageEntity::class
    ]
)
@TypeConverters(Converters::class)
internal abstract class ChatDatabase : RoomDatabase() {

    abstract val chatDao: ChatDao

    companion object {

        private const val DB_FILE_NAME = "chat_storage"

        fun build(context: Context): ChatDatabase {
            return Room.databaseBuilder(context, ChatDatabase::class.java, DB_FILE_NAME).build()
        }
    }
}