package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserLocation

interface UserLocationPort {
    fun save(location: UserLocation)

    fun loadByGroupId(groupId: GroupId): List<UserLocation>

    fun deleteByGroupIdAndUserId(groupId: GroupId, userId: UserId)

    fun deleteByGroupId(groupId: GroupId)
}
