package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.UserId
import com.example.haeautoeverstudy.application.domain.model.UserLocation

interface UserLocationPort {
    fun save(location: UserLocation)

    fun loadByUserIds(userIds: Set<UserId>): List<UserLocation>

    fun deleteByUserId(userId: UserId)

    fun deleteByUserIds(userIds: Set<UserId>)
}
