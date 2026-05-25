package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserLocation

fun interface GetGroupUserLocationsUseCase {
    fun get(command: GetGroupUserLocationsCommand): List<UserLocation>
}

data class GetGroupUserLocationsCommand(
    val groupId: GroupId,
)
