package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.GeoLocation
import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface UpdateUserLocationUseCase {
    fun update(command: UpdateUserLocationCommand)
}

data class UpdateUserLocationCommand(
    val userId: UserId,
    val groupId: GroupId,
    val location: GeoLocation,
)
