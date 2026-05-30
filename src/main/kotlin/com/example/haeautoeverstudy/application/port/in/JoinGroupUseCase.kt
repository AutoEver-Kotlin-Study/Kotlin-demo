package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface JoinGroupUseCase {
    fun join(command: JoinGroupCommand)
}

data class JoinGroupCommand(
    val userId: UserId,
    val groupId: GroupId,
)
