package com.revolut.kompot.sample.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.revolut.kompot.sample.data.database.entity.ChatEntity
import com.revolut.kompot.sample.data.database.entity.ContactEntity
import com.revolut.kompot.sample.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contactRow: ContactEntity)

    @Insert
    suspend fun insertMessage(messageRow: MessageEntity)

    @Transaction
    suspend fun insertNewMessage(contactRow: ContactEntity, messageRow: MessageEntity) {
        insertContact(contactRow)
        insertMessage(messageRow)
    }

    @Query("SELECT s.id, s.firstName, s.lastName, s.avatar, m1.message as lastMessage, m1.timestamp as lastMessageDate, m3.unreadCount FROM contacts s JOIN messages m1 ON (s.id = m1.senderId OR s.id = m1.receiverId) LEFT OUTER JOIN messages m2 ON ((s.id = m2.senderId OR s.id = m2.receiverId) AND  (m1.timestamp < m2.timestamp OR (m1.timestamp = m2.timestamp AND m1.id < m2.id))) LEFT JOIN (SELECT COUNT(*) as unreadCount, senderId FROM messages WHERE isRead = 0 GROUP BY senderId) as m3 ON s.id = m3.senderId WHERE m2.id IS NULL ORDER BY lastMessageDate DESC;")
    fun chatListStream(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM messages WHERE senderId = :contactId OR receiverId = :contactId")
    fun messagesStream(contactId: Long): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isRead = 1 WHERE senderId = :contactId")
    suspend fun markMessagesAsRead(contactId: Long)

    @Query("SELECT * FROM contacts")
    fun contactsStream(): Flow<List<ContactEntity>>

}