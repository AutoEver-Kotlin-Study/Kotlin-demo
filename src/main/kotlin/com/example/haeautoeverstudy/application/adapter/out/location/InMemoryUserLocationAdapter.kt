package com.example.haeautoeverstudy.application.adapter.out.location

import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserLocation
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryUserLocationAdapter : UserLocationPort {
    private val locations = ConcurrentHashMap<UserId, UserLocation>()

    override fun save(location: UserLocation) {
        locations[location.userId] = location
    }

    override fun loadByUserIds(userIds: Set<UserId>): List<UserLocation> =
        userIds.mapNotNull(locations::get)

    override fun deleteByUserId(userId: UserId) {
        locations.remove(userId)
    }
}
