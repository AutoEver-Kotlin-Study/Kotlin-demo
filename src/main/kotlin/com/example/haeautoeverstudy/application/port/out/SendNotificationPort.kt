package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.UserId

interface SendNotificationPort {
    val channelType: NotificationChannelType

    suspend fun send(command: SendNotificationCommand)
}

data class SendNotificationCommand(
    val recipientId: UserId,
    val channel: NotificationChannel,
    val message: NotificationMessage,
)

enum class NotificationChannelType {
    SMS,
    EMAIL,
}

sealed interface NotificationChannel {
    val type: NotificationChannelType

    data class Sms(val phoneNumber: PhoneNumber) : NotificationChannel {
        override val type: NotificationChannelType = NotificationChannelType.SMS
    }
}

data class NotificationMessage(
    val title: String,
    val body: String,
)
