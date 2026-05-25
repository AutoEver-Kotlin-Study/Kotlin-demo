package com.example.haeautoeverstudy.application.adapter.out.location

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserLocation
import com.example.haeautoeverstudy.application.port.out.UserLocationPort
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryUserLocationAdapter : UserLocationPort {
    private val locationsByGroupId = ConcurrentHashMap<GroupId, ConcurrentHashMap<UserId, UserLocation>>()

    override fun save(location: UserLocation) {
        locationsByGroupId
            .computeIfAbsent(location.groupId) { ConcurrentHashMap() }[location.userId] = location
    }

    override fun loadByGroupId(groupId: GroupId): List<UserLocation> =
        locationsByGroupId[groupId]?.values?.toList().orEmpty()

    override fun deleteByGroupIdAndUserId(groupId: GroupId, userId: UserId) {
        locationsByGroupId[groupId]?.remove(userId)
    }

    override fun deleteByGroupId(groupId: GroupId) {
        locationsByGroupId.remove(groupId)
    }
}
