package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.GeoLocation
import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.GroupName
import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserLocation
import com.example.haeautoeverstudy.application.domain.model.exception.NonParticipantAccessException
import com.example.haeautoeverstudy.application.port.`in`.GetGroupUserLocationsCommand
import com.example.haeautoeverstudy.application.port.`in`.UpdateUserLocationCommand
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupPort
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserLocationServiceTest {
    private val fixedInstant = Instant.parse("2026-05-25T00:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    @Test
    fun `updates location only when user is group participant`() {
        val groupId = GroupId("group")
        val userId = UserId("participant")
        val group = group(groupId)
        group.addParticipant(userId)
        val locationPort = RecordingUserLocationPort()
        val service = service(groups = mapOf(groupId to group), userLocationPort = locationPort)

        service.update(
            UpdateUserLocationCommand(
                userId = userId,
                groupId = groupId,
                location = GeoLocation(latitude = 37.5665, longitude = 126.9780),
            ),
        )

        assertEquals(
            UserLocation(
                groupId = groupId,
                userId = userId,
                location = GeoLocation(latitude = 37.5665, longitude = 126.9780),
                updatedAt = fixedInstant,
            ),
            locationPort.savedLocations.single(),
        )
    }

    @Test
    fun `rejects location update from non participant`() {
        val groupId = GroupId("group")
        val service = service(groups = mapOf(groupId to group(groupId)))

        assertFailsWith<NonParticipantAccessException> {
            service.update(
                UpdateUserLocationCommand(
                    userId = UserId("stranger"),
                    groupId = groupId,
                    location = GeoLocation(latitude = 37.5665, longitude = 126.9780),
                ),
            )
        }
    }

    @Test
    fun `gets only current visible group participant locations`() {
        val groupId = GroupId("group")
        val requesterId = UserId("requester")
        val visibleUserId = UserId("visible")
        val leftUserId = UserId("left")
        val group = group(groupId, ownerId = requesterId)
        group.addParticipant(visibleUserId)
        val locationPort = RecordingUserLocationPort()
        locationPort.save(location(groupId, visibleUserId))
        locationPort.save(location(groupId, leftUserId))
        val service = service(groups = mapOf(groupId to group), userLocationPort = locationPort)

        val result = service.get(
            GetGroupUserLocationsCommand(
                groupId = groupId,
            ),
        )

        assertEquals(listOf(location(groupId, visibleUserId)), result)
    }

    private fun service(
        groups: Map<GroupId, MapGroup>,
        userLocationPort: RecordingUserLocationPort = RecordingUserLocationPort(),
    ): UserLocationService =
        UserLocationService(
            loadMapGroupPort = FakeLoadMapGroupPort(groups),
            userLocationPort = userLocationPort,
            clock = clock,
        )

    private class FakeLoadMapGroupPort(private val groups: Map<GroupId, MapGroup>) : LoadMapGroupPort {
        override fun loadById(groupId: GroupId): MapGroup = groups.getValue(groupId)
    }

    private class RecordingUserLocationPort : UserLocationPort {
        private val locations = linkedMapOf<UserId, UserLocation>()
        val savedLocations = mutableListOf<UserLocation>()

        override fun save(location: UserLocation) {
            savedLocations += location
            locations[location.userId] = location
        }

        override fun loadByGroupId(groupId: GroupId): List<UserLocation> =
            locations.values.filter { it.groupId == groupId }

        override fun deleteByGroupIdAndUserId(groupId: GroupId, userId: UserId) {
            locations.remove(userId)
        }

        override fun deleteByGroupId(groupId: GroupId) {
            locations.entries.removeIf { it.value.groupId == groupId }
        }
    }

    private fun group(
        groupId: GroupId,
        ownerId: UserId = UserId("owner"),
    ): MapGroup =
        MapGroup.of(
            id = groupId,
            ownerId = ownerId,
            name = GroupName("family"),
            maxParticipantCount = 5,
        )

    private fun location(groupId: GroupId, userId: UserId): UserLocation =
        UserLocation(
            groupId = groupId,
            userId = userId,
            location = GeoLocation(latitude = 37.5665, longitude = 126.9780),
            updatedAt = fixedInstant,
        )
}
