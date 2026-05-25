package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface DeleteGroupUseCase {
    fun delete(command: DeleteGroupCommand)
}

data class DeleteGroupCommand(
    val groupId: GroupId,
    val requestedBy: UserId,
)
