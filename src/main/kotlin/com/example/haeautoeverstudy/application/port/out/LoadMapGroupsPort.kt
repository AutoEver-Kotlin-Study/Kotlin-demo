package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.MapGroup
import com.example.haeautoeverstudy.application.domain.model.UserId

interface LoadMapGroupsPort {
    fun loadAllByIds(groupIds: Set<GroupId>): List<MapGroup>

    fun loadActiveByParticipantId(userId: UserId): List<MapGroup>
}
