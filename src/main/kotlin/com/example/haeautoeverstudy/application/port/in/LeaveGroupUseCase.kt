package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface LeaveGroupUseCase {
    fun leave(command: LeaveGroupCommand)
}

data class LeaveGroupCommand(
    val userId: UserId,
    val groupId: GroupId,
)
