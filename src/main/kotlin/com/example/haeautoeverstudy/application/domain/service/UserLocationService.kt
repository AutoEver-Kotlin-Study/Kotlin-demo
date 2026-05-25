package com.example.haeautoeverstudy.application.domain.service

import com.example.haeautoeverstudy.application.domain.model.UserLocation
import com.example.haeautoeverstudy.application.port.`in`.GetGroupUserLocationsCommand
import com.example.haeautoeverstudy.application.port.`in`.GetGroupUserLocationsUseCase
import com.example.haeautoeverstudy.application.port.`in`.UpdateUserLocationCommand
import com.example.haeautoeverstudy.application.port.`in`.UpdateUserLocationUseCase
import com.example.haeautoeverstudy.application.port.out.LoadMapGroupPort
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class UserLocationService(
    private val loadMapGroupPort: LoadMapGroupPort,
    private val userLocationPort: UserLocationPort,
    private val clock: Clock = Clock.systemUTC(),
) : UpdateUserLocationUseCase, GetGroupUserLocationsUseCase {

    @Transactional(readOnly = true)
    override fun update(command: UpdateUserLocationCommand) {
        val group = loadMapGroupPort.loadById(command.groupId)
        group.assertParticipant(command.userId)

        userLocationPort.save(
            UserLocation(
                groupId = command.groupId,
                userId = command.userId,
                location = command.location,
                updatedAt = Instant.now(clock),
            ),
        )
    }

    @Transactional(readOnly = true)
    override fun get(command: GetGroupUserLocationsCommand): List<UserLocation> {
        val group = loadMapGroupPort.loadById(command.groupId)
        val participantIds = group.participants

        return userLocationPort.loadByGroupId(command.groupId)
            .filter { it.userId in participantIds }
    }
}
