package com.example.haeautoeverstudy.application.port.`in`

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.GroupName
import com.example.haeautoeverstudy.application.domain.model.UserId

fun interface GetUserGroupsUseCase {
    fun getGroups(command: GetUserGroupsCommand): List<GroupSummary>
}

data class GetUserGroupsCommand(
    val userId: UserId,
)

data class GroupSummary(
    val groupId: GroupId,
    val ownerId: UserId,
    val name: GroupName,
    val maxParticipantCount: Int,
    val currentParticipantCount: Int,
)
