package com.example.haeautoeverstudy.application.port.out

import com.example.haeautoeverstudy.application.domain.model.GroupId
import com.example.haeautoeverstudy.application.domain.model.MapGroup

interface LoadMapGroupPort {
    fun loadById(groupId: GroupId): MapGroup
}
