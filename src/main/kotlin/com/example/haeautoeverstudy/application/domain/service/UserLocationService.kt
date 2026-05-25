package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.UserLocation
import com.example.haeautoeverstudy.application.port.`in`.GetGroupUserLocationsCommand
import com.example.haeautoeverstudy.application.port.`in`.GetGroupUserLocationsUseCase
import com.example.haeautoeverstudy.application.port.`in`.UpdateUserLocationCommand
import com.example.haeautoeverstudy.application.port.`in`.UpdateUserLocationUseCase
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupPort
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class UserLocationService(
    private val loadMapGroupPort: LoadMapGroupPort,
    private val userLocationPort: UserLocationPort,
    private val clock: Clock = Clock.systemUTC(),
) : UpdateUserLocationUseCase, GetGroupUserLocationsUseCase {

    override fun update(command: UpdateUserLocationCommand) {
        val group = loadMapGroupPort.loadById(command.groupId)
        group.assertParticipant(command.userId)

        userLocationPort.save(
            UserLocation(
                userId = command.userId,
                location = command.location,
                updatedAt = Instant.now(clock),
            ),
        )
    }

    override fun get(command: GetGroupUserLocationsCommand): List<UserLocation> {
        val group = loadMapGroupPort.loadById(command.groupId)
        val visibleUserIds = group.visibleParticipantIdsFor(command.requesterId)

        return userLocationPort.loadByUserIds(visibleUserIds)
            .filter { it.userId in visibleUserIds }
    }
}
