package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.MapGroupEvent
import com.example.haeautoeverstudy.application.domain.model.PhoneNumber
import com.example.haeautoeverstudy.application.domain.model.User
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserName
import com.example.haeautoeverstudy.application.port.out.LoadUserPort
import com.example.haeautoeverstudy.application.port.out.NotificationChannel
import com.example.haeautoeverstudy.application.port.out.NotificationChannelType
import com.example.haeautoeverstudy.application.port.out.SendNotificationCommand
import com.example.haeautoeverstudy.application.port.out.SendNotificationPort
import org.junit.jupiter.api.DisplayName
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MapGroupNotificationServiceTest {

    @Test
    fun `sends sms notifications to event recipients`() {
        val recipientIds = setOf(UserId("first"), UserId("second"))
        val loadUserPort = FakeLoadUserPort(
            recipientIds.associateWith { userId ->
                user(userId.value)
            },
        )
        val smsPort = RecordingSmsNotificationPort()
        val emailPort = RecordingEmailNotificationPort()
        val service = MapGroupNotificationService(loadUserPort, listOf(emailPort, smsPort))

        service.handle(
            MapGroupEvent.ParticipantJoined(
                groupId = GroupId("group"),
                joinedUserId = UserId("joined"),
                notificationRecipientIds = recipientIds,
            ),
        )

        assertEquals(2, smsPort.commands.size)
        assertEquals(0, emailPort.commands.size)
        smsPort.commands.forEach { command ->
            assertIs<NotificationChannel.Sms>(command.channel)
            assertEquals("Group participant joined", command.message.title)
        }
    }

    private class FakeLoadUserPort(private val users: Map<UserId, User>) : LoadUserPort {
        override fun loadById(userId: UserId): User = users.getValue(userId)
    }

    private class RecordingSmsNotificationPort : SendNotificationPort {
        override val channelType: NotificationChannelType = NotificationChannelType.SMS
        val commands: MutableList<SendNotificationCommand> = Collections.synchronizedList(mutableListOf())

        override suspend fun send(command: SendNotificationCommand) {
            commands += command
        }
    }

    private class RecordingEmailNotificationPort : SendNotificationPort {
        override val channelType: NotificationChannelType = NotificationChannelType.EMAIL
        val commands: MutableList<SendNotificationCommand> = Collections.synchronizedList(mutableListOf())

        override suspend fun send(command: SendNotificationCommand) {
            commands += command
        }
    }

    private fun user(id: String): User =
        User.of(
            id = UserId(id),
            name = UserName("${id}User"),
            phoneNumber = PhoneNumber("01012345678"),
        )
}
