package com.revolut.kompot.sample.feature.chat.data.messenger

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.di.ChatsApiProvider
import com.revolut.kompot.sample.feature.chat.domain.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

private val messageDelay = 1000L..2000L

class MessengerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val chatRepository: ChatRepository by lazy(LazyThreadSafetyMode.NONE) {
        ChatsApiProvider.component.chatRepository
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val senderId = if (intent?.hasExtra(KEY_SENDER_ID) == true) {
            intent.getLongExtra(KEY_SENDER_ID, 0)
        } else {
            null
        }
        simulateMessage(startId, senderId)
        return START_STICKY
    }

    private fun simulateMessage(startId: Int, senderId: Long? = null) = scope.launch {
        val sender = if (senderId == null) {
            contacts.random()
        } else {
            contacts.first { it.id == senderId }
        }
        val messageText = messagePhrases.random()
        delay(Random.nextLong(messageDelay.first, messageDelay.last))
        val message = Message.createToUser(sender.id, messageText, Date())
        chatRepository.saveMessage(sender, message)
        stopSelf(startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        scope.cancel()
    }

    companion object {

        private const val KEY_SENDER_ID = "sender_id"

        fun getGenerateMessagesIntent(context: Context, senderId: Long? = null): Intent {
            val intent = Intent(context, MessengerService::class.java)
            if (senderId != null) {
                intent.putExtra(KEY_SENDER_ID, senderId)
            }
            return intent
        }

    }

}