package com.example.haeautoeverstudy.application.domain.model

sealed interface MapGroupEvent {
    val groupId: GroupId
    val notificationRecipientIds: Set<UserId>

    data class ParticipantJoined(
        override val groupId: GroupId,
        val joinedUserId: UserId,
        override val notificationRecipientIds: Set<UserId>,
    ) : MapGroupEvent

    data class ParticipantLeft(
        override val groupId: GroupId,
        val leftUserId: UserId,
        override val notificationRecipientIds: Set<UserId>,
    ) : MapGroupEvent

    data class GroupDeleted(
        override val groupId: GroupId,
        val deletedBy: UserId,
        override val notificationRecipientIds: Set<UserId>,
    ) : MapGroupEvent
}
