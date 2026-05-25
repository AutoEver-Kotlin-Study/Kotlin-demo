package com.example.haeautoeverstudy.application.domain.model

import com.example.haeautoeverstudy.application.domain.model.exception.GroupCapacityExceededException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupDeletionForbiddenException
import com.example.haeautoeverstudy.application.domain.model.exception.GroupOwnerCannotLeaveException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidGroupNameException
import com.example.haeautoeverstudy.application.domain.model.exception.InvalidMaxParticipantCountException
import com.example.haeautoeverstudy.application.domain.model.exception.NonParticipantAccessException
import com.example.haeautoeverstudy.application.domain.model.exception.ParticipantAlreadyExistsException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapGroupTest {
    @Test
    fun `creates group with owner as first participant`() {
        val ownerId = UserId("owner")

        val group = MapGroup.of(
            id = GroupId("group"),
            ownerId = ownerId,
            name = GroupName("family"),
            maxParticipantCount = 3,
        )

        assertEquals(GroupId("group"), group.id)
        assertEquals(ownerId, group.ownerId)
        assertEquals(GroupName("family"), group.name)
        assertEquals(3, group.maxParticipantCount)
        assertEquals(setOf(ownerId), group.participants)
        assertFalse(group.isDeleted)
    }

    @Test
    fun `rejects invalid group name`() {
        assertFailsWith<InvalidGroupNameException> {
            GroupName("")
        }

        assertFailsWith<InvalidGroupNameException> {
            GroupName("abcd")
        }
    }

    @Test
    fun `rejects invalid max participant count`() {
        assertFailsWith<InvalidMaxParticipantCountException> {
            MapGroup.of(
                id = GroupId("group"),
                ownerId = UserId("owner"),
                name = GroupName("family"),
                maxParticipantCount = 0,
            )
        }
    }

    @Test
    fun `adds participant and returns joined event`() {
        val group = group(maxParticipantCount = 2)
        val participantId = UserId("participant")

        val event = group.addParticipant(participantId)

        assertTrue(participantId in group.participants)
        assertEquals(participantId, event.joinedUserId)
        assertEquals(setOf(UserId("owner")), event.notificationRecipientIds)
    }

    @Test
    fun `rejects duplicate participant`() {
        val group = group(maxParticipantCount = 2)
        val participantId = UserId("participant")

        group.addParticipant(participantId)

        assertFailsWith<ParticipantAlreadyExistsException> {
            group.addParticipant(participantId)
        }
    }

    @Test
    fun `rejects participant when group is full`() {
        val group = group(maxParticipantCount = 1)

        assertFailsWith<GroupCapacityExceededException> {
            group.addParticipant(UserId("participant"))
        }
    }

    @Test
    fun `owner cannot leave group`() {
        val group = group(maxParticipantCount = 2)

        assertFailsWith<GroupOwnerCannotLeaveException> {
            group.removeParticipant(UserId("owner"))
        }
    }

    @Test
    fun `only owner can delete group`() {
        val group = group(maxParticipantCount = 2)

        assertFailsWith<GroupDeletionForbiddenException> {
            group.delete(UserId("participant"))
        }
    }

    @Test
    fun `only participants can view other participant ids`() {
        val group = group(maxParticipantCount = 2)

        assertFailsWith<NonParticipantAccessException> {
            group.visibleParticipantIdsFor(UserId("stranger"))
        }
    }

    private fun group(maxParticipantCount: Int): MapGroup =
        MapGroup.of(
            id = GroupId("group"),
            ownerId = UserId("owner"),
            name = GroupName("family"),
            maxParticipantCount = maxParticipantCount,
        )
}
