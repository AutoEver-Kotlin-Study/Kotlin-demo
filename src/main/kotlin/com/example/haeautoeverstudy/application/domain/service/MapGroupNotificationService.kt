package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent
import com.example.haeautoeverstudy.application.port.`in`.HandleMapGroupEventUseCase
import com.example.haeautoeverstudy.application.port.out.LoadUserPort
import com.example.haeautoeverstudy.application.port.out.NotificationChannel
import com.example.haeautoeverstudy.application.port.out.NotificationMessage
import com.example.haeautoeverstudy.application.port.out.SendNotificationCommand
import com.example.haeautoeverstudy.application.port.out.SendNotificationPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class MapGroupNotificationService(
    private val loadUserPort: LoadUserPort,
    sendNotificationPorts: List<SendNotificationPort>,
) : HandleMapGroupEventUseCase {
    private val sendNotificationPortByChannelType = sendNotificationPorts.associateBy { it.channelType }


    override fun handle(event: MapGroupEvent) = runBlocking {
        val message = event.toNotificationMessage()

        event.notificationRecipientIds
            .map { recipientId ->
                async(Dispatchers.IO) {
                    val recipient = loadUserPort.loadById(recipientId)

                    send(
                        SendNotificationCommand(
                            recipientId = recipient.id,
                            channel = NotificationChannel.Sms(recipient.phoneNumber),
                            message = message,
                        ),
                    )
                }
            }
            .awaitAll()

        Unit
    }

    private suspend fun send(command: SendNotificationCommand) {
        val sender = sendNotificationPortByChannelType[command.channel.type]
            ?: throw IllegalStateException("No notification sender supports ${command.channel.type}")

        sender.send(command)
    }

    private fun MapGroupEvent.toNotificationMessage(): NotificationMessage =
        when (this) {
            is MapGroupEvent.ParticipantJoined -> NotificationMessage(
                title = "Group participant joined",
                body = "A new participant joined your group.",
            )

            is MapGroupEvent.ParticipantLeft -> NotificationMessage(
                title = "Group participant left",
                body = "A participant left your group.",
            )

            is MapGroupEvent.GroupDeleted -> NotificationMessage(
                title = "Group deleted",
                body = "Your group has been deleted.",
            )
        }
}
