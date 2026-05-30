package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.GroupName
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface CreateGroupUseCase {
    fun create(command: CreateGroupCommand): GroupDetail
}

data class CreateGroupCommand(
    val groupId: GroupId,
    val ownerId: UserId,
    val name: GroupName,
    val maxParticipantCount: Int,
)

data class GroupDetail(
    val groupId: GroupId,
    val ownerId: UserId,
    val name: GroupName,
    val maxParticipantCount: Int,
    val currentParticipantCount: Int,
)
