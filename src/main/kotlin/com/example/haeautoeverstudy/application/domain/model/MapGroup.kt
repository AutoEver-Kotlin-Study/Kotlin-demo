package com.example.haeautoeverstudy.application.domain.model

import com.example.haeautoeverstudy.application.domain.model.exception.GroupAlreadyDeletedException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupCapacityExceededException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupDeletionForbiddenException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupOwnerCannotLeaveException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidGroupNameException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidMaxParticipantCountException
import com.example.haeautoeverstudy.application.domain.model.exception.NonParticipantAccessException
import com.example.haeautoeverstudy.application.domain.model.exception.ParticipantAlreadyExistsException
import com.example.haeautoeverstudy.application.domain.model.exception.ParticipantNotFoundException
import java.util.UUID

@JvmInline
value class GroupId(val value: String) {
    init {
        require(value.isNotBlank()) { "GroupId cannot be blank" }
    }

    companion object {
        fun withUUID(): GroupId = GroupId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class GroupName(val value: String) {
    init {
        if (value.isBlank()) {
            throw InvalidGroupNameException()
        }

        if (value.length >= 32 || value.length <= 4) {
            throw InvalidGroupNameException()
        }
    }
}

class MapGroup private constructor(
    val id: GroupId,
    val ownerId: UserId,
    val name: GroupName,
    val maxParticipantCount: Int,
    private val participantIds: LinkedHashSet<UserId>,
    private var deleted: Boolean,
) {
    init {
        if (maxParticipantCount <= 0) {
            throw InvalidMaxParticipantCountException(maxParticipantCount)
        }

        if (participantIds.isEmpty()) {
            throw ParticipantNotFoundException(id.value, ownerId.value)
        }

        if (ownerId !in participantIds) {
            throw NonParticipantAccessException(id.value, ownerId.value)
        }

        if (participantIds.size > maxParticipantCount) {
            throw GroupCapacityExceededException(id.value, maxParticipantCount)
        }
    }

    val isDeleted: Boolean
        get() = deleted

    val participants: Set<UserId>
        get() = participantIds.toSet()

    val currentParticipantCount: Int
        get() = participantIds.size

    fun addParticipant(userId: UserId): MapGroupEvent.ParticipantJoined {
        ensureActive()

        if (userId in participantIds) {
            throw ParticipantAlreadyExistsException(id.value, userId.value)
        }

        if (participantIds.size >= maxParticipantCount) {
            throw GroupCapacityExceededException(id.value, maxParticipantCount)
        }

        val recipients = participantIds.toSet()
        participantIds += userId

        return MapGroupEvent.ParticipantJoined(
            groupId = id,
            joinedUserId = userId,
            notificationRecipientIds = recipients,
        )
    }

    fun removeParticipant(userId: UserId): MapGroupEvent.ParticipantLeft {
        ensureActive()
        assertParticipant(userId)

        if (userId == ownerId) {
            throw GroupOwnerCannotLeaveException(id.value, userId.value)
        }

        participantIds -= userId

        return MapGroupEvent.ParticipantLeft(
            groupId = id,
            leftUserId = userId,
            notificationRecipientIds = participantIds.toSet(),
        )
    }

    fun delete(requestedBy: UserId): MapGroupEvent.GroupDeleted {
        ensureActive()

        if (requestedBy != ownerId) {
            throw GroupDeletionForbiddenException(id.value, requestedBy.value)
        }

        deleted = true

        return MapGroupEvent.GroupDeleted(
            groupId = id,
            deletedBy = requestedBy,
            notificationRecipientIds = participantIds
                .filterNot { it == requestedBy }
                .toSet(),
        )
    }

    fun assertParticipant(userId: UserId) {
        ensureActive()

        if (userId !in participantIds) {
            throw NonParticipantAccessException(id.value, userId.value)
        }
    }

    fun visibleParticipantIdsFor(requesterId: UserId): Set<UserId> {
        assertParticipant(requesterId)
        return participantIds.filterNot { it == requesterId }.toSet()
    }

    private fun ensureActive() {
        if (deleted) {
            throw GroupAlreadyDeletedException(id.value)
        }
    }

    //팩토리 메서드
    companion object {
        fun of(
            ownerId: UserId,
            name: GroupName,
            maxParticipantCount: Int,
            id: GroupId = GroupId.withUUID(),
        ): MapGroup = MapGroup(
            id = id,
            ownerId = ownerId,
            name = name,
            maxParticipantCount = maxParticipantCount,
            participantIds = linkedSetOf(ownerId),
            deleted = false,
        )
    }
}
