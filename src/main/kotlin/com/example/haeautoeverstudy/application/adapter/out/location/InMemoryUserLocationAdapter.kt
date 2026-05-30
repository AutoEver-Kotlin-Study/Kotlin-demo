package com.example.haeautoeverstudy.application.adapter.out.location

import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserLocation
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryUserLocationAdapter : UserLocationPort {
    private val locationsByUserId = ConcurrentHashMap<UserId, UserLocation>()

    override fun save(location: UserLocation) {
        locationsByUserId[location.userId] = location
    }

    override fun loadByUserIds(userIds: Set<UserId>): List<UserLocation> =
        userIds.mapNotNull(locationsByUserId::get)

    override fun deleteByUserId(userId: UserId) {
        locationsByUserId.remove(userId)
    }

    override fun deleteByUserIds(userIds: Set<UserId>) {
        userIds.forEach(locationsByUserId::remove)
    }
}
